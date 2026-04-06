package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.dto.SeckillOrderDTO;
import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_pojo.vo.OrderVO;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_pojo.vo.SeckillOrderVO;

public interface OrderService {

    /**
     * 秒杀下单核心入口
     * <p>
     * 流程：缓存预热兜底 → Lua 原子校验（防超卖 + 一人一单）→ 生成订单号
     * → 立即返回 VO → 异步落库（不阻塞响应）
     *
     * @param dto 前端提交的秒杀商品 + 收货信息
     * @return 立即响应 VO（包含 orderNo，status=0 待支付）
     */
    SeckillOrderVO placeSeckillOrder(SeckillOrderDTO dto);

    /**
     * 根据订单号查询用户自己的秒杀订单详情
     *
     * @param orderNo 全局唯一订单号
     * @return 订单 VO（不存在或不属于当前用户则抛异常）
     */
    OrderVO getOrderByNo(String orderNo);

    /**
     * 分页查询当前用户的秒杀订单列表（最新在前），返回 { list, total } 格式
     *
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 分页结果，total 为该用户秒杀订单总数
     */
    PageResult<OrderVO> listMyOrders(int page, int pageSize);

    /**
     * 用户主动取消待支付订单
     * <p>
     * 仅允许取消 status = 0（待支付）的本人订单，取消后回补 Redis + DB 库存，
     * 从 bought Set 移除记录（允许用户重新参与秒杀），并恢复已使用的优惠券。
     *
     * @param orderNo 订单号
     */
    void cancelOrder(String orderNo);

    /**
     * Mock 支付接口
     * <p>
     * 校验订单归属当前用户且状态为 0（待支付），满足条件则更新 status=1（已支付）+ payTime=now。
     * 无需真实支付密码校验，任意密码均视为通过（课程 Demo 级模拟）。
     *
     * @param orderNo 订单号
     */
    void mockPay(String orderNo);

    /**
     * 秒杀库存预热
     * <p>
     * 将 DB 中 seckill_product.seckill_stock_init 写入 Redis（NX 保证幂等）。
     * 在活动开始前主动调用可保证 Redis 命中；placeSeckillOrder 内部也会惰性兜底调用，
     * 防止极端情况下 Redis 数据丢失导致秒杀失败。
     *
     * @param spId 秒杀商品 ID
     */
    void warmUpSeckillStock(Long spId);

    /**
     * 取消订单后资源回补：Redis 库存、DB 库存、bought Set、优惠券
     * <p>
     * 供用户主动取消（cancelOrder）及定时任务自动取消共用。
     *
     * @param order 已取消的订单实体（需包含 spId、userCouponId 等字段）
     */
    void restoreStockAndCoupon(Order order);
}
