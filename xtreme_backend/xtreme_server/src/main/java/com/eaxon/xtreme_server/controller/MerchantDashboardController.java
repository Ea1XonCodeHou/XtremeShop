package com.eaxon.xtreme_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.vo.MerchantDashboardOverviewVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.MerchantDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/merchant/dashboard")
@RequiredArgsConstructor
public class MerchantDashboardController {

    private final MerchantDashboardService merchantDashboardService;

    /**
     * 商家看板总览
     * GET /api/merchant/dashboard/overview
     */
    @GetMapping("/overview")
    public Result<MerchantDashboardOverviewVO> overview(
            @RequestParam(defaultValue = "5") int recentLimit) {
        if (recentLimit <= 0) recentLimit = 5;
        if (recentLimit > 20) recentLimit = 20;

        Long merchantId = BaseContext.getCurrentId();
        MerchantDashboardOverviewVO vo = merchantDashboardService.getOverview(merchantId, recentLimit);
        return Result.success(vo);
    }
}
