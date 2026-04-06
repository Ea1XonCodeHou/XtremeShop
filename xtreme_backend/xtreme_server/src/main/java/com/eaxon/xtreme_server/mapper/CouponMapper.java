package com.eaxon.xtreme_server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Coupon;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM coupon WHERE merchant_id = #{merchantId} AND is_active != 0 ORDER BY created_at DESC")
    List<Coupon> selectByMerchantId(Long merchantId);

    @Insert("INSERT INTO coupon(merchant_id, name, type, discount_value, min_order_amount, total_count, used_count, get_count, start_time, end_time, created_at, is_active) " +
            "VALUES(#{merchantId}, #{name}, #{type}, #{discountValue}, #{minOrderAmount}, #{totalCount}, 0, 0, #{startTime}, #{endTime}, #{createdAt}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCoupon(Coupon coupon);

    /**
     * 原子领券：get_count + 1，条件为活动时间内 & 有效 & 未超发
     * 返回 1 = 成功，0 = 已抢完或不在有效期
     */
    @Update("UPDATE coupon SET get_count = get_count + 1 WHERE id = #{couponId} " +
            "AND is_active = 1 AND NOW() BETWEEN start_time AND end_time " +
            "AND (total_count = 0 OR get_count < total_count)")
    int claimCoupon(@Param("couponId") Long couponId);

    /** 核销时已用数 + 1 */
    @Update("UPDATE coupon SET used_count = used_count + 1 WHERE id = #{couponId}")
    int incrementUsedCount(@Param("couponId") Long couponId);

    /** 取消订单时已用数回退（最小值 0，防止减为负数） */
    @Update("UPDATE coupon SET used_count = GREATEST(used_count - 1, 0) WHERE id = #{couponId}")
    int decrementUsedCount(@Param("couponId") Long couponId);
}
