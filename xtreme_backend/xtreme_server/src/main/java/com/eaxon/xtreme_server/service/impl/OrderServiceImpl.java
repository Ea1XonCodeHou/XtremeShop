package com.eaxon.xtreme_server.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.eaxon.xtreme_common.utils.RedisIdWorker;
import com.eaxon.xtreme_pojo.dto.SeckillOrderDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_pojo.vo.OrderVO;
import com.eaxon.xtreme_pojo.vo.SeckillOrderVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;
import com.eaxon.xtreme_server.service.OrderAsyncService;
import com.eaxon.xtreme_server.service.OrderService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 秒杀下单核心服务实现
 *
 * <p>核心设计（高并发防超卖）：
 * <ol>
 *   <li>缓存预热（warmUpSeckillStock）：活动开始前将 DB 库存写入 Redis，NX 保证幂等</li>
 *   <li>Lua 原子校验（placeSeckillOrder）：GET stock + SISMEMBER bought + DECR + SADD，单次 RTT 内完成</li>
 *   <li>立即返回：Lua 通过后立刻组装 VO 响应，响应时间 ≈ Redis RTT（< 5ms）</li>
 *   <li>异步落库：将 DB 写入交给 OrderAsyncService + seckillOrderExecutor 线程池后台完成</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** Redis Key 前缀 */
    private static final String SECKILL_STOCK_KEY_PREFIX  = "seckill:stock:";
    private static final String SECKILL_BOUGHT_KEY_PREFIX = "seckill:bought:";

    /** 库存预热 TTL（24小时，覆盖最长活动周期） */
    private static final long STOCK_TTL_HOURS = 24L;

    private final StringRedisTemplate redisTemplate;
    private final RedisIdWorker        redisIdWorker;
    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper        productMapper;
    private final OrderMapper          orderMapper;
    private final OrderAsyncService    orderAsyncService;

    /** 预加载的 Lua 脚本对象（应用启动时初始化一次） */
    private RedisScript<Long> seckillScript;

    // ------------------------------------------------------------------ //
    //  初始化
    // ------------------------------------------------------------------ //

    @PostConstruct
    private void init() {
        ClassPathResource resource = new ClassPathResource("lua/seckill.lua");
        if (!resource.exists()) {
            throw new IllegalStateException("Lua 脚本未找到: classpath:lua/seckill.lua");
        }
        seckillScript = RedisScript.of(resource, Long.class);
        log.info("秒杀 Lua 脚本加载成功");
    }

    // ------------------------------------------------------------------ //
    //  库存预热（关键！活动开始前必须完成，此处同时作为惰性兜底）
    // ------------------------------------------------------------------ //

    /**
     * 秒杀库存预热：将 seckill_product.seckill_stock_init 写入 Redis。
     * <p>
     * 使用 SET NX（setIfAbsent）确保幂等：
     * - 若 key 未存在，首次写入初始库存；
     * - 若 key 已存在（已预热或已有扣减），跳过，避免重置正在进行中的库存。
     * <p>
     * 调用时机：
     * <ul>
     *   <li>推荐：活动上线前由运营/定时任务批量调用</li>
     *   <li>兜底：{@link #placeSeckillOrder} 在执行 Lua 前自动调用本方法</li>
     * </ul>
     */
    @Override
    public void warmUpSeckillStock(Long spId) {
        String stockKey = SECKILL_STOCK_KEY_PREFIX + spId;

        // 快速路径：key 已存在则直接返回，不查 DB
        if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
            return;
        }

        SeckillProduct sp = seckillProductMapper.selectById(spId);
        if (sp == null) {
            throw new RuntimeException("秒杀商品不存在：spId=" + spId);
        }

        // SET NX EX：仅当 key 不存在时写入，TTL 24h
        boolean set = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(
                        stockKey,
                        String.valueOf(sp.getSeckillStockInit()),
                        STOCK_TTL_HOURS, TimeUnit.HOURS
                )
        );

        if (set) {
            log.info("秒杀库存预热完成 - spId: {}, initStock: {}", spId, sp.getSeckillStockInit());
        } else {
            log.debug("秒杀库存预热跳过（并发写入已由其他线程完成）- spId: {}", spId);
        }
    }

    // ------------------------------------------------------------------ //
    //  秒杀下单（核心路径）
    // ------------------------------------------------------------------ //

    @Override
    public SeckillOrderVO placeSeckillOrder(SeckillOrderDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Long spId   = dto.getSeckillProductId();

        // Step 1 ▶ 缓存预热兜底（应在活动前提前预热；此处防止极端漏网之鱼）
        warmUpSeckillStock(spId);

        // Step 2 ▶ 执行 Lua 原子操作：防超卖 + 一人一单
        String stockKey  = SECKILL_STOCK_KEY_PREFIX  + spId;
        String boughtKey = SECKILL_BOUGHT_KEY_PREFIX + spId;
        Long result = redisTemplate.execute(
                seckillScript,
                Arrays.asList(stockKey, boughtKey),
                String.valueOf(userId)
        );

        if (result == null || result == -1L) {
            throw new RuntimeException("手慢了，库存已抢完！");
        }
        if (result == -2L) {
            throw new RuntimeException("每人限购一件，您已参与过此秒杀活动");
        }

        // Step 2.5 ▶ 为 bought Set 补充 TTL（防止永久占用 Redis，与库存 key 同生命周期）
        // 使用 expireIfAbsent 语义：仅在 TTL 未设置时写入，避免每次下单都重置过期时间
        Long boughtTtl = redisTemplate.getExpire(boughtKey, TimeUnit.HOURS);
        if (boughtTtl == null || boughtTtl < 0) {
            redisTemplate.expire(boughtKey, STOCK_TTL_HOURS, TimeUnit.HOURS);
        }

        // Step 3 ▶ 查询商品信息（组装 VO 用，也传给异步落库）
        SeckillProduct sp = seckillProductMapper.selectById(spId);
        if (sp == null) {
            throw new RuntimeException("秒杀商品数据异常，请联系客服");
        }
        Product product = productMapper.selectById(sp.getProductId());
        if (product == null) {
            throw new RuntimeException("关联商品数据异常，请联系客服");
        }

        // Step 4 ▶ 生成全局唯一订单号
        String orderNo = String.valueOf(redisIdWorker.nextId("order"));
        LocalDateTime now = LocalDateTime.now();

        // Step 5 ▶ 组装立即响应 VO（在异步落库完成前客户端可先展示）
        SeckillOrderVO vo = new SeckillOrderVO();
        vo.setOrderNo(orderNo);
        vo.setProductName(product.getName());
        vo.setCoverUrl(product.getCoverUrl());
        vo.setSeckillPrice(sp.getSeckillPrice());
        vo.setActualAmount(sp.getSeckillPrice());   // qty=1，暂无优惠券折扣
        vo.setQuantity(1);
        vo.setCreatedAt(now);
        vo.setStatus(0);                             // 待支付

        // Step 6 ▶ 异步落库（提交到 seckillOrderExecutor 线程池，不阻塞响应）
        // 注意：OrderAsyncService 是独立 Bean，Spring AOP 代理生效，@Async 正常工作
        orderAsyncService.saveOrder(orderNo, userId, sp, product, now);

        log.info("秒杀抢购成功 - orderNo: {}, userId: {}, spId: {}", orderNo, userId, spId);
        return vo;
    }

    // ------------------------------------------------------------------ //
    //  订单查询
    // ------------------------------------------------------------------ //

    @Override
    public OrderVO getOrderByNo(String orderNo) {
        Long userId = BaseContext.getCurrentId();
        OrderVO vo = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (vo == null) {
            throw new RuntimeException("订单不存在或无权查看");
        }
        return vo;
    }

    @Override
    public List<OrderVO> listMyOrders(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();
        int offset = (page - 1) * pageSize;
        return orderMapper.selectSeckillOrdersByUserId(userId, pageSize, offset);
    }
}
