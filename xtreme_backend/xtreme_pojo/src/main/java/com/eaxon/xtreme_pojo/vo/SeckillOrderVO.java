package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 秒杀下单立即响应 VO
 * Redis Lua 校验通过后立即返回，无需等待异步落库完成
 */
@Data
public class SeckillOrderVO {

    /** 全局唯一订单号（用于后续查询订单状态） */
    private String orderNo;

    /** 商品名称 */
    private String productName;

    /** 商品封面 */
    private String coverUrl;

    /** 实付金额 */
    private BigDecimal actualAmount;

    /** 秒杀单价 */
    private BigDecimal seckillPrice;

    /** 购买数量（秒杀固定 1）*/
    private Integer quantity;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 订单初始状态（0 = 待支付）
     * 此时订单已进入落库队列，前端可通过 orderNo 轮询获取最终状态
     */
    private Integer status;
}
