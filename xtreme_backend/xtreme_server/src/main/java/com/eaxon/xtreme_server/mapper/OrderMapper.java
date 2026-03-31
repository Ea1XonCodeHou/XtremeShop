package com.eaxon.xtreme_server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_pojo.vo.OrderVO;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询指定用户的秒杀订单列表（JOIN seckill_product + product 获取商品信息）
     * 按创建时间倒序
     */
    @Select("SELECT o.id AS orderId, o.order_no AS orderNo, o.status, " +
            "o.original_price AS originalPrice, o.seckill_price AS seckillPrice, " +
            "o.actual_amount AS actualAmount, o.quantity, o.created_at AS createdAt, " +
            "p.name AS productName, p.cover_url AS coverUrl, " +
            "sa.name AS activityName " +
            "FROM `order` o " +
            "JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN seckill_product sp ON o.seckill_product_id = sp.id " +
            "LEFT JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE o.user_id = #{userId} AND o.seckill_product_id IS NOT NULL " +
            "ORDER BY o.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<OrderVO> selectSeckillOrdersByUserId(@Param("userId") Long userId,
                                              @Param("limit") int limit,
                                              @Param("offset") int offset);

    /**
     * 根据订单号查询（附带商品信息）
     */
    @Select("SELECT o.id AS orderId, o.order_no AS orderNo, o.status, " +
            "o.original_price AS originalPrice, o.seckill_price AS seckillPrice, " +
            "o.actual_amount AS actualAmount, o.quantity, o.created_at AS createdAt, " +
            "p.name AS productName, p.cover_url AS coverUrl, " +
            "sa.name AS activityName " +
            "FROM `order` o " +
            "JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN seckill_product sp ON o.seckill_product_id = sp.id " +
            "LEFT JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE o.order_no = #{orderNo} AND o.user_id = #{userId}")
    OrderVO selectByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                     @Param("userId") Long userId);
}
