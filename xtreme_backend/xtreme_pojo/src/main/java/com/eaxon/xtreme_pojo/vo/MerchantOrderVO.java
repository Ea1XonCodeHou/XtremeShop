package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 商家端订单列表 VO
 * 包含订单核心信息、商品信息、收货信息，供商家侧分页展示
 */
@Data
public class MerchantOrderVO {

    private Long orderId;

    private String orderNo;

    /** 商品名称（JOIN product 得到） */
    private String productName;

    /** 商品封面图 */
    private String coverUrl;

    /** 秒杀活动名（LEFT JOIN seckill_activity，NULL 表示普通订单） */
    private String activityName;

    /** 商品原价快照 */
    private BigDecimal originalPrice;

    /** 秒杀/成交单价 */
    private BigDecimal seckillPrice;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 购买数量 */
    private Integer quantity;

    /**
     * 订单状态
     * 0 = 待支付  1 = 已支付  2 = 已取消  3 = 已退款
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 收货人姓名 */
    private String receiver;

    /** 收货人手机号（来自 order.phone，下单时填写） */
    private String phone;

    /** 收货地址 */
    private String address;
}
