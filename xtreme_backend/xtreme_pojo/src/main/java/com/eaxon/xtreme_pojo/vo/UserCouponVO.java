package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 用户侧优惠券展示 VO
 * 由 user_coupon JOIN coupon 查询得到，包含券详情和领取/使用状态
 */
@Data
public class UserCouponVO {

    /** user_coupon 记录 ID，下单时作为 userCouponId 传入 */
    private Long id;

    /** coupon 表主键 */
    private Long couponId;

    /** 券名称 */
    private String name;

    /**
     * 券类型
     * 1 = 满减券  2 = 折扣券
     */
    private Integer type;

    /**
     * 优惠值
     * 满减券：减免金额（元）；折扣券：折扣率（如 0.8 = 八折）
     */
    private BigDecimal discountValue;

    /** 最低消费门槛（0 = 无门槛） */
    private BigDecimal minOrderAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 使用状态（来自 user_coupon.status）
     * 0 = 未使用  1 = 已使用  2 = 已过期
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime getTime;
}
