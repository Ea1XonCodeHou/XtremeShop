package com.eaxon.xtreme_server.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.xtreme_common.utils.RedisIdWorker;
import com.eaxon.xtreme_pojo.dto.SeckillOrderDTO;
import com.eaxon.xtreme_pojo.entity.Coupon;
import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_pojo.entity.UserCoupon;
import com.eaxon.xtreme_pojo.vo.OrderVO;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_pojo.vo.SeckillOrderVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.mapper.CouponMapper;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;
import com.eaxon.xtreme_server.mapper.UserCouponMapper;
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
    private final UserCouponMapper     userCouponMapper;
    private final CouponMapper         couponMapper;

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
        // 注意：使用 seckillStock（当前 DB 剩余库存），而非 seckillStockInit（初始值）
        // 理由：若 Redis Key 已过期（如活动进行中 Redis 重启），用剩余库存重建，防止超卖
        boolean set = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(
                        stockKey,
                        String.valueOf(sp.getSeckillStock()),
                        STOCK_TTL_HOURS, TimeUnit.HOURS
                )
        );

        if (set) {
            log.info("秒杀库存预热完成 - spId: {}, currentStock: {}", spId, sp.getSeckillStock());
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

        // Step 4 ▶ 优惠券校验与核销（同步执行，确保在响应前状态已锁定）
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long userCouponId = dto.getUserCouponId();
        if (userCouponId != null) {
            // 4.1 校验 user_coupon 归属 & 状态
            UserCoupon uc = userCouponMapper.selectByIdAndUserId(userCouponId, userId);
            if (uc == null) {
                throw new RuntimeException("优惠券不存在");
            }
            if (uc.getStatus() != 0) {
                throw new RuntimeException("优惠券已使用或已过期");
            }

            // 4.2 校验 coupon 有效性
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            LocalDateTime now2 = LocalDateTime.now();
            if (coupon == null || coupon.getIsActive() != 1
                    || now2.isBefore(coupon.getStartTime()) || now2.isAfter(coupon.getEndTime())) {
                throw new RuntimeException("优惠券已失效");
            }

            // 4.3 校验最低消费门槛
            if (sp.getSeckillPrice().compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new RuntimeException("未达到优惠券使用门槛 ¥" + coupon.getMinOrderAmount());
            }

            // 4.4 原子核销（WHERE status = 0 防并发重复使用）
            int marked = userCouponMapper.markUsed(userCouponId);
            if (marked == 0) {
                throw new RuntimeException("优惠券核销失败，请重试");
            }
            couponMapper.incrementUsedCount(coupon.getId());

            // 4.5 计算折扣金额
            if (coupon.getType() == 1) {
                // 满减：直接减免固定金额
                discountAmount = coupon.getDiscountValue();
            } else if (coupon.getType() == 2) {
                // 折扣：seckillPrice × (1 - discountValue)，保留两位小数
                BigDecimal discount = sp.getSeckillPrice().multiply(
                        BigDecimal.ONE.subtract(coupon.getDiscountValue())
                ).setScale(2, RoundingMode.HALF_UP);
                discountAmount = discount;
            }
            // 折扣不可超过实际秒杀价
            discountAmount = discountAmount.min(sp.getSeckillPrice());
        }

        // Step 5 ▶ 计算最终金额
        BigDecimal actualAmount = sp.getSeckillPrice().subtract(discountAmount);

        // Step 6 ▶ 生成全局唯一订单号
        String orderNo = String.valueOf(redisIdWorker.nextId("order"));
        LocalDateTime now = LocalDateTime.now();

        // Step 7 ▶ 组装立即响应 VO（在异步落库完成前客户端可先展示）
        SeckillOrderVO vo = new SeckillOrderVO();
        vo.setOrderNo(orderNo);
        vo.setProductName(product.getName());
        vo.setCoverUrl(product.getCoverUrl());
        vo.setSeckillPrice(sp.getSeckillPrice());
        vo.setActualAmount(actualAmount);
        vo.setQuantity(1);
        vo.setCreatedAt(now);
        vo.setStatus(0);                             // 待支付

        // Step 8 ▶ 异步落库（提交到 seckillOrderExecutor 线程池，不阻塞响应）
        orderAsyncService.saveOrder(orderNo, userId, sp, product, now,
                dto.getReceiver(), dto.getPhone(), dto.getAddress(),
                userCouponId, discountAmount, actualAmount);

        log.info("秒杀抢购成功 - orderNo: {}, userId: {}, spId: {}, discount: {}",
                orderNo, userId, spId, discountAmount);
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
    public PageResult<OrderVO> listMyOrders(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();
        int offset = (page - 1) * pageSize;
        List<OrderVO> list = orderMapper.selectSeckillOrdersByUserId(userId, pageSize, offset);
        long total = orderMapper.countSeckillOrdersByUserId(userId);
        return PageResult.of(list, total);
    }

    @Override
    public void cancelOrder(String orderNo) {
        Long userId = BaseContext.getCurrentId();

        // 先查询订单（用于取消后的库存/优惠券回补）
        Order order = orderMapper.selectEntityByOrderNoAndUserId(orderNo, userId);
        if (order == null) {
            throw new RuntimeException("订单不存在或无权操作");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("只有待支付订单才能取消");
        }

        // 原子取消（WHERE status = 0 防并发重复操作）
        int updated = orderMapper.cancelOrder(orderNo, userId);
        if (updated == 0) {
            throw new RuntimeException("取消失败，订单状态已变更");
        }

        // 回补秒杀库存与 Redis
        restoreStockAndCoupon(order);

        log.info("用户取消订单成功 - orderNo: {}, userId: {}", orderNo, userId);
    }

    /**
     * 取消订单后的资源回补：Redis 库存、DB 库存、bought Set、优惠券
     * 供用户主动取消和定时任务自动取消共用
     */
    @Override
    public void restoreStockAndCoupon(Order order) {
        Long spId = order.getSeckillProductId();
        if (spId != null) {
            // 仅当 Redis key 存在时 INCR，避免 key 过期后被凭空创建错误值
            String stockKey = SECKILL_STOCK_KEY_PREFIX + spId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
                redisTemplate.opsForValue().increment(stockKey);
            }
            // 从 bought Set 移除，允许用户重新参与秒杀
            redisTemplate.opsForSet().remove(
                    SECKILL_BOUGHT_KEY_PREFIX + spId,
                    String.valueOf(order.getUserId())
            );
            // 同步回补 DB 库存
            seckillProductMapper.incrementStock(spId);
        }

        // 恢复优惠券（仅当 user_coupon.status == 1 才回滚，防幂等问题）
        Long userCouponId = order.getUserCouponId();
        if (userCouponId != null) {
            UserCoupon uc = userCouponMapper.selectById(userCouponId);
            if (uc != null && uc.getStatus() == 1) {
                userCouponMapper.restoreUserCoupon(userCouponId);
                couponMapper.decrementUsedCount(uc.getCouponId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(String orderNo) {
        Long userId = BaseContext.getCurrentId();
        int updated = orderMapper.mockPay(orderNo, userId);
        if (updated == 0) {
            // 可能原因：订单不存在、不属于当前用户、状态不是待支付
            throw new RuntimeException("支付失败：订单不存在、无权操作或已支付");
        }

        Order paidOrder = orderMapper.selectEntityByOrderNoAndUserId(orderNo, userId);
        if (paidOrder == null) {
            throw new RuntimeException("支付失败：订单不存在或无权操作");
        }

        int quantity = paidOrder.getQuantity() == null || paidOrder.getQuantity() <= 0
                ? 1
                : paidOrder.getQuantity();

        // 支付成功后更新商品销售指标：优先销量+库存同步，库存不足时仍保证销量可统计。
        int metricUpdated = productMapper.increaseSoldCountAndDecreaseStock(paidOrder.getProductId(), quantity);
        if (metricUpdated == 0) {
            productMapper.increaseSoldCountOnly(paidOrder.getProductId(), quantity);
            log.warn("商品普通库存不足，仅更新销量统计 - orderNo: {}, productId: {}, quantity: {}",
                    orderNo, paidOrder.getProductId(), quantity);
        }

        log.info("Mock支付成功 - orderNo: {}, userId: {}", orderNo, userId);
    }
}
