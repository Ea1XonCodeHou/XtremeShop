package com.eaxon.xtreme_server.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 秒杀订单异步落库服务
 * <p>
 * 必须独立为单独的 Spring Bean（不能放在 OrderServiceImpl 内部方法），
 * 否则同类自调用绕过 Spring AOP 代理，@Async 不生效。
 * <p>
 * Redis Lua 校验通过后，由 OrderServiceImpl 调用此 Bean 的方法，
 * 由 seckillOrderExecutor 线程池异步完成 MySQL 写入，不阻塞接口响应。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAsyncService {

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;

    /**
     * 将秒杀订单异步写入 MySQL
     * <p>
     * 参数由调用方（OrderServiceImpl）在主线程中已查询并传入，
     * 避免异步线程再次访问 ThreadLocal（BaseContext 在异步线程中无效）。
     *
     * @param orderNo   全局唯一订单号
     * @param userId    下单用户 ID
     * @param sp        秒杀商品实体（含 activityId, productId, seckillPrice）
     * @param product   商品实体（含 merchantId, price, name）
     * @param createdAt 下单时间（与立即响应 VO 保持一致）
     * @param receiver  收货人姓名
     * @param phone     收货手机号
     * @param address   收货地址
     */
    @Async("seckillOrderExecutor")
    public void saveOrder(String orderNo, Long userId,
                          SeckillProduct sp, Product product,
                          LocalDateTime createdAt,
                          String receiver, String phone, String address) {
        try {
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setMerchantId(product.getMerchantId());
            order.setProductId(product.getId());
            order.setSeckillProductId(sp.getId());
            order.setQuantity(1);                            // 秒杀固定 1 件
            order.setOriginalPrice(product.getPrice());      // 原价快照
            order.setSeckillPrice(sp.getSeckillPrice());     // 秒杀价
            order.setDiscountAmount(BigDecimal.ZERO);        // 暂不支持叠加优惠券
            order.setActualAmount(sp.getSeckillPrice());     // 实付 = 秒杀价 × 1
            order.setReceiver(receiver);
            order.setPhone(phone);
            order.setAddress(address);
            order.setStatus(0);                              // 0 = 待支付
            order.setCreatedAt(createdAt);
            order.setUpdatedAt(createdAt);

            orderMapper.insert(order);
            // 异步回写 DB 库存，保证数据库与 Redis 最终一致（Lua 已原子扣减 Redis，此处仅同步 DB）
            int updated = seckillProductMapper.decrementStock(sp.getId());
            if (updated == 0) {
                log.warn("秒杀DB库存回写未成功（库存已为0？） - spId: {}, orderNo: {}", sp.getId(), orderNo);
            }
            log.info("秒杀订单落库成功 - orderNo: {}, userId: {}, spId: {}",
                    orderNo, userId, sp.getId());
        } catch (Exception e) {
            // 落库失败记录错误日志，后续可接入告警/补偿队列
            log.error("秒杀订单落库失败 - orderNo: {}, userId: {}, error: {}",
                    orderNo, userId, e.getMessage(), e);
        }
    }
}
