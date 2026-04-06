package com.eaxon.xtreme_server.task;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 秒杀订单超时自动取消定时任务
 * <p>
 * 每 60 秒扫描一次处于 status=0（待支付）且创建时间超过 15 分钟的历史订单，
 * 逐条通过原子 UPDATE 尝试取消，成功后回补 Redis 库存 + DB 库存 + 优惠券。
 * <p>
 * 注意：每批最多处理 100 条（见 OrderMapper#selectExpiredPendingOrders），
 * 若积压量过大，下次定时任务触发时会继续消化剩余待处理订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderScheduleTask {

    private final OrderMapper  orderMapper;
    private final OrderService orderService;

    /**
     * fixedDelay 保证上一批处理完成后再等待 60 秒才开始下一批，
     * 避免并发执行时出现重复取消。
     */
    @Scheduled(fixedDelay = 60_000)
    public void autoCancelExpiredOrders() {
        List<Order> expiredOrders = orderMapper.selectExpiredPendingOrders();
        if (expiredOrders.isEmpty()) {
            return;
        }
        log.info("超时自动取消定时任务 - 待处理订单数: {}", expiredOrders.size());

        int cancelledCount = 0;
        for (Order order : expiredOrders) {
            // 原子 UPDATE WHERE status=0，防止并发多实例重复取消
            int affected = orderMapper.cancelByIdIfPending(order.getId());
            if (affected == 1) {
                try {
                    orderService.restoreStockAndCoupon(order);
                    cancelledCount++;
                } catch (Exception e) {
                    log.error("超时取消订单回补资源失败 - orderId: {}, orderNo: {}",
                            order.getId(), order.getOrderNo(), e);
                }
            }
        }
        log.info("超时自动取消定时任务完成 - 成功取消: {} / {}", cancelledCount, expiredOrders.size());
    }
}
