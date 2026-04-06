package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 商家看板：近期订单项
 */
@Data
public class MerchantRecentOrderVO {

    /** 订单号 */
    private String orderNo;

    /** 商品名称 */
    private String productName;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 订单状态：0待支付 1已支付 2已取消 3已退款 */
    private Integer status;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
