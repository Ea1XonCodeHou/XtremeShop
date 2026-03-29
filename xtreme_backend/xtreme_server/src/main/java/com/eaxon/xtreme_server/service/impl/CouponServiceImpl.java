package com.eaxon.xtreme_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eaxon.xtreme_pojo.dto.CouponDTO;
import com.eaxon.xtreme_pojo.entity.Coupon;
import com.eaxon.xtreme_server.mapper.CouponMapper;
import com.eaxon.xtreme_server.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;

    @Override
    public List<Coupon> listByMerchant(Long merchantId) {
        return couponMapper.selectByMerchantId(merchantId);
    }

    @Override
    public void create(Long merchantId, CouponDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setMerchantId(merchantId);
        coupon.setName(dto.getName());
        coupon.setType(dto.getType() != null ? dto.getType() : 1);
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinOrderAmount(dto.getMinOrderAmount() != null ? dto.getMinOrderAmount() : java.math.BigDecimal.ZERO);
        coupon.setTotalCount(dto.getTotalCount() != null ? dto.getTotalCount() : 0);
        coupon.setStartTime(dto.getStartTime());
        coupon.setEndTime(dto.getEndTime());
        coupon.setCreatedAt(LocalDateTime.now());
        couponMapper.insertCoupon(coupon);
    }

    @Override
    public void delete(Long merchantId, Long couponId) {
        couponMapper.delete(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getId, couponId)
                        .eq(Coupon::getMerchantId, merchantId)
        );
    }
}
