package com.eaxon.xtreme_server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.vo.UserCouponVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.UserCouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户侧优惠券接口
 * <pre>
 *   GET  /api/user/coupons          查询我的全部优惠券列表
 *   POST /api/user/coupons/{id}     领取指定优惠券（coupon.id）
 * </pre>
 * 以上接口均受 UserInterceptor 拦截（/api/user/** 路径），需携带会话 Header。
 */
@Slf4j
@RestController
@RequestMapping("/api/user/coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;

    /** 查询当前用户的全部优惠券（含已使用/已过期，前端自行过滤展示） */
    @GetMapping
    public Result<List<UserCouponVO>> listMyCoupons() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(userCouponService.listMyCoupons(userId));
    }

    /**
     * 领取优惠券
     *
     * @param couponId coupon 表主键（非 user_coupon），即商家发布的券 ID
     */
    @PostMapping("/{couponId}")
    public Result<Void> claimCoupon(@PathVariable Long couponId) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户领券 - userId: {}, couponId: {}", userId, couponId);
        userCouponService.claimCoupon(userId, couponId);
        return Result.success();
    }
}
