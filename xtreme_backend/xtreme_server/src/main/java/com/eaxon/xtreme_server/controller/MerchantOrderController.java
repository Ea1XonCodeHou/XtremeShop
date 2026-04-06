package com.eaxon.xtreme_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.vo.MerchantOrderVO;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.MerchantOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 商家端订单接口
 * <pre>
 *   GET /api/merchant/orders   分页查询当前商家的订单列表
 *     参数：
 *       page      - 页码，默认 1
 *       pageSize  - 每页条数，默认 10，最大 50
 *       status    - 订单状态（0 待付款 / 1 已付款 / 2 已取消 / 3 已退款），不传表示全部
 * </pre>
 * 受 MerchantInterceptor 拦截（/api/merchant/** 路径），需携带商家会话 Header。
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    @GetMapping
    public Result<PageResult<MerchantOrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        // 限制单页最大条数，防止过量查询
        if (pageSize > 50) {
            pageSize = 50;
        }
        Long merchantId = BaseContext.getCurrentId();
        log.info("商家查询订单 - merchantId: {}, page: {}, pageSize: {}, status: {}", merchantId, page, pageSize, status);
        return Result.success(merchantOrderService.listOrders(merchantId, status, page, pageSize));
    }
}
