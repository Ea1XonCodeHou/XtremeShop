package com.eaxon.xtreme_server.service;

import java.util.List;

import com.eaxon.xtreme_pojo.vo.UserCouponVO;

/**
 * 用户侧优惠券服务接口
 */
public interface UserCouponService {

    /**
     * 用户领取优惠券
     *
     * @param userId   当前用户 ID
     * @param couponId 目标券 ID（coupon 表主键）
     */
    void claimCoupon(Long userId, Long couponId);

    /**
     * 查询用户的全部优惠券（含已使用/已过期）
     *
     * @param userId 当前用户 ID
     */
    List<UserCouponVO> listMyCoupons(Long userId);
}
