package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 订单列表 / 订单详情 VO
 * 用于用户「我的订单」页和订单状态查询接口
 */
@Data
public class OrderVO {

    /** 订单 DB 主键 */
    private Long orderId;

    /** 全局订单号 */
    private String orderNo;

    /** 商品名称（JOIN product 获取） */
    private String productName;

    /** 商品封面图（JOIN product 获取） */
    private String coverUrl;

    /** 所属秒杀活动名（LEFT JOIN seckill_activity 获取，NULL 表示普通订单） */
    private String activityName;

    /** 商品原价快照 */
    private BigDecimal originalPrice;

    /** 成交单价（秒杀价） */
    private BigDecimal seckillPrice;

    /** 实付总价 */
    private BigDecimal actualAmount;

    /** 购买数量 */
    private Integer quantity;

    /**
     * 订单状态
     * 0 = 待支付  1 = 已支付  2 = 已取消  3 = 已退款
     */
    private Integer status;

    /** 支付时间（待支付时为 null） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
