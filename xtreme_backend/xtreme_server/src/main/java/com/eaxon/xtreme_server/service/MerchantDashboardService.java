package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.vo.MerchantDashboardOverviewVO;

public interface MerchantDashboardService {

    /**
     * 获取商家看板总览
     *
     * @param merchantId 商家 ID
     * @param recentLimit 近期订单数量上限
     */
    MerchantDashboardOverviewVO getOverview(Long merchantId, int recentLimit);
}
