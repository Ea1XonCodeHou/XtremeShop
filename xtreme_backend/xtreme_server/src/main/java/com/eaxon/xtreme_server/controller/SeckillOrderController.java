package com.eaxon.xtreme_server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.SeckillOrderDTO;
import com.eaxon.xtreme_pojo.vo.OrderVO;
import com.eaxon.xtreme_pojo.vo.SeckillOrderVO;
import com.eaxon.xtreme_server.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户端秒杀下单控制器
 * <p>
 * 所有接口均需用户登录（UserInterceptor 在 WebMvcConfig 中对 /api/seckill/order/** 和
 * /api/seckill/orders 启用）。
 * <p>
 * 接口一览：
 * <pre>
 *   POST /api/seckill/order              秒杀下单（< 5ms，Lua 原子 + 异步落库）
 *   GET  /api/seckill/order/{orderNo}    查询订单详情
 *   GET  /api/seckill/orders             分页查询我的秒杀订单列表
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillOrderController {

    private final OrderService orderService;

    /**
     * 秒杀下单
     * <p>
     * Redis Lua 原子校验通过后立即返回，订单异步写入 MySQL。
     * 响应包含 orderNo，前端可凭此轮询最终订单状态。
     */
    @PostMapping("/order")
    public Result<SeckillOrderVO> placeSeckillOrder(@RequestBody SeckillOrderDTO dto) {
        log.info("秒杀下单请求 - spId: {}", dto.getSeckillProductId());
        SeckillOrderVO vo = orderService.placeSeckillOrder(dto);
        return Result.success(vo);
    }

    /**
     * 查询订单详情（含落库完成后的最终状态）
     * <p>
     * 若 placeSeckillOrder 刚刚返回即轮询，异步落库可能尚未完成，
     * 此时此接口返回 404；建议前端延迟 300ms 后首次轮询。
     */
    @GetMapping("/order/{orderNo}")
    public Result<OrderVO> getOrderByNo(@PathVariable String orderNo) {
        OrderVO vo = orderService.getOrderByNo(orderNo);
        return Result.success(vo);
    }

    /**
     * 分页查询当前用户的秒杀订单列表（最新在前）
     *
     * @param page     页码，从 1 开始，默认 1
     * @param pageSize 每页条数，默认 10，最大 50
     */
    @GetMapping("/orders")
    public Result<List<OrderVO>> listMyOrders(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageSize > 50) pageSize = 50;
        List<OrderVO> orders = orderService.listMyOrders(page, pageSize);
        return Result.success(orders);
    }
}
