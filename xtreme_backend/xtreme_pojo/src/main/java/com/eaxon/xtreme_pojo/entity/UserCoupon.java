package com.eaxon.xtreme_pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户优惠券实体（对应 user_coupon 表）
 * 记录用户领取和使用券的状态
 */
@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID（关联 user.id） */
    private Long userId;

    /** 优惠券 ID（关联 coupon.id） */
    private Long couponId;

    /**
     * 使用状态
     * 0 = 未使用  1 = 已使用  2 = 已过期
     */
    private Integer status;

    /** 领取时间 */
    private LocalDateTime getTime;

    /** 使用时间（核销时更新） */
    private LocalDateTime useTime;
}
