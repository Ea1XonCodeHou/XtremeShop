package com.eaxon.xtreme_server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.UserCoupon;
import com.eaxon.xtreme_pojo.vo.UserCouponVO;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户的所有优惠券（JOIN coupon 获取详情），按领取时间倒序
     */
    @Select("SELECT uc.id, uc.coupon_id AS couponId, c.name, c.type, " +
            "c.discount_value AS discountValue, c.min_order_amount AS minOrderAmount, " +
            "c.start_time AS startTime, c.end_time AS endTime, uc.status, uc.get_time AS getTime " +
            "FROM user_coupon uc JOIN coupon c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} AND c.is_active = 1 " +
            "ORDER BY uc.get_time DESC")
    List<UserCouponVO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据 user_coupon.id 和 userId 查询（安全校验：防止操作他人券）
     */
    @Select("SELECT * FROM user_coupon WHERE id = #{id} AND user_id = #{userId}")
    UserCoupon selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 检查用户是否已领取过该券（防重领）
     */
    @Select("SELECT COUNT(*) FROM user_coupon WHERE user_id = #{userId} AND coupon_id = #{couponId}")
    int countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 原子标记为已使用（WHERE status = 0 防并发重复使用）
     * 返回 1 = 成功，0 = 已使用或不存在
     */
    @Update("UPDATE user_coupon SET status = 1, use_time = NOW() WHERE id = #{id} AND status = 0")
    int markUsed(@Param("id") Long id);

    /**
     * 取消订单时恢复优惠券为未使用状态（WHERE status = 1 确保幂等）
     */
    @Update("UPDATE user_coupon SET status = 0, use_time = NULL WHERE id = #{id} AND status = 1")
    int restoreUserCoupon(@Param("id") Long id);
}
