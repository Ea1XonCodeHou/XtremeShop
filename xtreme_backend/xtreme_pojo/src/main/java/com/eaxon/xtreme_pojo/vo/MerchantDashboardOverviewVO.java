package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 商家看板总览数据
 */
@Data
public class MerchantDashboardOverviewVO {

    /** 已支付订单实收总额 */
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    /** 已支付订单总数 */
    private Integer totalOrders = 0;

    /** 在售商品数 */
    private Integer totalProducts = 0;

    /** 近期订单 */
    private List<MerchantRecentOrderVO> recentOrders = new ArrayList<>();
}
