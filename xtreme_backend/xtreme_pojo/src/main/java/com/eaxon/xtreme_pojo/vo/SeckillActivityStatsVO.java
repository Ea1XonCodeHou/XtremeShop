package com.eaxon.xtreme_pojo.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 商家端某秒杀活动的订单汇总统计
 */
@Data
public class SeckillActivityStatsVO implements Serializable {

    private Long activityId;

    private String activityName;

    /** 该活动下本商家的商品总数 */
    private int productCount;

    /** 该活动下本商家的总订单数 */
    private int totalOrders;

    /** 已支付订单数 */
    private int paidOrders;

    /** 已取消订单数 */
    private int cancelledOrders;

    /** 已支付订单总金额 */
    private BigDecimal totalPaidAmount;
}
