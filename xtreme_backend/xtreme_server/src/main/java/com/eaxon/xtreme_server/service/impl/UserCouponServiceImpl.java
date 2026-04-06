package com.eaxon.xtreme_server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eaxon.xtreme_pojo.entity.UserCoupon;
import com.eaxon.xtreme_pojo.vo.UserCouponVO;
import com.eaxon.xtreme_server.mapper.CouponMapper;
import com.eaxon.xtreme_server.mapper.UserCouponMapper;
import com.eaxon.xtreme_server.service.UserCouponService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponMapper userCouponMapper;
    private final CouponMapper couponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimCoupon(Long userId, Long couponId) {
        // 防重领：同一用户不可重复领取同一张券
        if (userCouponMapper.countByUserAndCoupon(userId, couponId) > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }

        // 原子领取：CAS 更新 get_count（超出发行量 / 过期 / 停用 → 返回 0）
        int affected = couponMapper.claimCoupon(couponId);
        if (affected == 0) {
            throw new RuntimeException("优惠券已抢完或不在有效期内");
        }

        // 写入用户优惠券记录
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        uc.setGetTime(LocalDateTime.now());
        userCouponMapper.insert(uc);

        log.info("用户领券完成 - userId: {}, couponId: {}", userId, couponId);
    }

    @Override
    public List<UserCouponVO> listMyCoupons(Long userId) {
        return userCouponMapper.selectByUserId(userId);
    }
}
