package com.eaxon.xtreme_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Coupon;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT * FROM coupon WHERE merchant_id = #{merchantId} AND is_active != 0 ORDER BY created_at DESC")
    List<Coupon> selectByMerchantId(Long merchantId);

    @Insert("INSERT INTO coupon(merchant_id, name, type, discount_value, min_order_amount, total_count, used_count, get_count, start_time, end_time, created_at, is_active) " +
            "VALUES(#{merchantId}, #{name}, #{type}, #{discountValue}, #{minOrderAmount}, #{totalCount}, 0, 0, #{startTime}, #{endTime}, #{createdAt}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCoupon(Coupon coupon);
}
