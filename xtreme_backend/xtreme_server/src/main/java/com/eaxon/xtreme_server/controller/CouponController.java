package com.eaxon.xtreme_server.controller;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.CouponDTO;
import com.eaxon.xtreme_pojo.entity.Coupon;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/merchant/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public Result<List<Coupon>> list() {
        Long merchantId = BaseContext.getCurrentId();
        return Result.success(couponService.listByMerchant(merchantId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody CouponDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        log.info("商家 {} 创建优惠券: {}", merchantId, dto.getName());
        couponService.create(merchantId, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long merchantId = BaseContext.getCurrentId();
        couponService.delete(merchantId, id);
        return Result.success();
    }
}
