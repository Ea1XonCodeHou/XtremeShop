package com.eaxon.xtreme_server.service;

import java.util.List;

import com.eaxon.xtreme_pojo.dto.SeckillOrderDTO;
import com.eaxon.xtreme_pojo.vo.OrderVO;
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
     * 分页查询当前用户的秒杀订单列表（最新在前）
     *
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 订单 VO 列表
     */
    List<OrderVO> listMyOrders(int page, int pageSize);

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
}
