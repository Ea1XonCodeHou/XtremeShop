package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.dto.CouponDTO;
import com.eaxon.xtreme_pojo.entity.Coupon;

import java.util.List;

public interface CouponService {
    List<Coupon> listByMerchant(Long merchantId);
    void create(Long merchantId, CouponDTO dto);
    void delete(Long merchantId, Long couponId);
}
