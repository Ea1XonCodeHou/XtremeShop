package com.eaxon.xtreme_server.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Order;
import com.eaxon.xtreme_pojo.vo.MerchantOrderVO;
import com.eaxon.xtreme_pojo.vo.MerchantRecentOrderVO;
import com.eaxon.xtreme_pojo.vo.OrderVO;
import com.eaxon.xtreme_pojo.vo.SeckillActivityStatsVO;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询指定用户的秒杀订单列表（JOIN seckill_product + product 获取商品信息）
     * 按创建时间倒序
     */
    @Select("SELECT o.id AS orderId, o.order_no AS orderNo, o.status, " +
            "o.original_price AS originalPrice, o.seckill_price AS seckillPrice, " +
            "o.actual_amount AS actualAmount, o.quantity, o.created_at AS createdAt, o.pay_time AS payTime, " +
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
            "o.actual_amount AS actualAmount, o.quantity, o.created_at AS createdAt, o.pay_time AS payTime, " +
            "p.name AS productName, p.cover_url AS coverUrl, " +
            "sa.name AS activityName " +
            "FROM `order` o " +
            "JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN seckill_product sp ON o.seckill_product_id = sp.id " +
            "LEFT JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE o.order_no = #{orderNo} AND o.user_id = #{userId}")
    OrderVO selectByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                     @Param("userId") Long userId);

    /**
     * Mock 支付：将订单状态更新为已支付，记录支付时间
     * 仅当订单归属当前用户且状态为 0（待支付）时才更新，防止越权和重复支付
     */
    @Update("UPDATE `order` SET status = 1, pay_time = NOW(), updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND user_id = #{userId} AND status = 0")
    int mockPay(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /**
     * 查询当前用户的秒杀订单总数（用于分页总数展示）
     */
    @Select("SELECT COUNT(*) FROM `order` WHERE user_id = #{userId} AND seckill_product_id IS NOT NULL")
    int countSeckillOrdersByUserId(@Param("userId") Long userId);

        /** 查询商家已支付订单总金额 */
        @Select("SELECT COALESCE(SUM(actual_amount), 0) FROM `order` WHERE merchant_id = #{merchantId} AND status = 1")
        BigDecimal sumPaidAmountByMerchant(@Param("merchantId") Long merchantId);

        /** 查询商家已支付订单总数 */
        @Select("SELECT COUNT(*) FROM `order` WHERE merchant_id = #{merchantId} AND status = 1")
        int countPaidOrdersByMerchant(@Param("merchantId") Long merchantId);

        /** 查询商家近期订单（最新在前） */
        @Select("SELECT o.order_no AS orderNo, p.name AS productName, o.actual_amount AS actualAmount, " +
                        "o.status AS status, o.created_at AS createdAt " +
                        "FROM `order` o JOIN product p ON o.product_id = p.id " +
                        "WHERE o.merchant_id = #{merchantId} " +
                        "ORDER BY o.created_at DESC LIMIT #{limit}")
        List<MerchantRecentOrderVO> selectRecentOrdersByMerchant(@Param("merchantId") Long merchantId,
                                                                                                                         @Param("limit") int limit);

        /** 查询订单原始实体（用于支付成功后的销量/库存更新） */
        @Select("SELECT * FROM `order` WHERE order_no = #{orderNo} AND user_id = #{userId}")
        Order selectEntityByOrderNoAndUserId(@Param("orderNo") String orderNo,
                                             @Param("userId") Long userId);

    // ------------------------------------------------------------------ //
    //  取消订单
    // ------------------------------------------------------------------ //

    /**
     * 用户主动取消：仅允许取消「待支付」状态（status = 0）的本人订单，
     * WHERE 条件含 status = 0 保证原子性，返回 1 = 成功，0 = 不可取消
     */
    @Update("UPDATE `order` SET status = 2, updated_at = NOW() " +
            "WHERE order_no = #{orderNo} AND user_id = #{userId} AND status = 0")
    int cancelOrder(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /**
     * 定时任务批量取消：按主键取消（已校验 status = 0）
     * 返回 1 = 成功（幂等，若已被用户取消则返回 0 跳过）
     */
    @Update("UPDATE `order` SET status = 2, updated_at = NOW() WHERE id = #{id} AND status = 0")
    int cancelByIdIfPending(@Param("id") Long id);

    /**
     * 查询超时未支付的订单（创建超 15 分钟且仍为 status = 0），每次最多取 100 条
     */
    @Select("SELECT * FROM `order` WHERE status = 0 AND created_at < DATE_SUB(NOW(), INTERVAL 15 MINUTE) LIMIT 100")
    List<Order> selectExpiredPendingOrders();

    // ------------------------------------------------------------------ //
    //  商家端订单查询
    // ------------------------------------------------------------------ //

    /**
     * 商家端全部订单分页（不过滤状态）
     */
    @Select("SELECT o.id AS orderId, o.order_no AS orderNo, o.status, " +
            "o.original_price AS originalPrice, o.seckill_price AS seckillPrice, " +
            "o.actual_amount AS actualAmount, o.quantity, " +
            "o.created_at AS createdAt, o.pay_time AS payTime, " +
            "o.receiver, o.phone, o.address, " +
            "p.name AS productName, p.cover_url AS coverUrl, " +
            "sa.name AS activityName " +
            "FROM `order` o " +
            "JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN seckill_product sp ON o.seckill_product_id = sp.id " +
            "LEFT JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE o.merchant_id = #{merchantId} " +
            "ORDER BY o.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<MerchantOrderVO> selectOrdersByMerchant(@Param("merchantId") Long merchantId,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset);

    /** 商家端总订单数（不过滤状态） */
    @Select("SELECT COUNT(*) FROM `order` WHERE merchant_id = #{merchantId}")
    long countOrdersByMerchant(@Param("merchantId") Long merchantId);

    /**
     * 商家端按状态过滤的订单分页
     */
    @Select("SELECT o.id AS orderId, o.order_no AS orderNo, o.status, " +
            "o.original_price AS originalPrice, o.seckill_price AS seckillPrice, " +
            "o.actual_amount AS actualAmount, o.quantity, " +
            "o.created_at AS createdAt, o.pay_time AS payTime, " +
            "o.receiver, o.phone, o.address, " +
            "p.name AS productName, p.cover_url AS coverUrl, " +
            "sa.name AS activityName " +
            "FROM `order` o " +
            "JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN seckill_product sp ON o.seckill_product_id = sp.id " +
            "LEFT JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status = #{status} " +
            "ORDER BY o.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<MerchantOrderVO> selectOrdersByMerchantAndStatus(@Param("merchantId") Long merchantId,
                                                           @Param("status") Integer status,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset);

    /** 商家端按状态过滤的订单总数 */
    @Select("SELECT COUNT(*) FROM `order` WHERE merchant_id = #{merchantId} AND status = #{status}")
    long countOrdersByMerchantAndStatus(@Param("merchantId") Long merchantId,
                                         @Param("status") Integer status);

    // ------------------------------------------------------------------ //
    //  商家端秒杀活动维度统计
    // ------------------------------------------------------------------ //

    /**
     * 查询指定秒杀活动下本商家的订单汇总统计
     * （仅统计 seckill_product_id 不为 NULL 的秒杀订单）
     */
    @Select("SELECT " +
            "  sa.id                           AS activityId, " +
            "  sa.name                         AS activityName, " +
            "  COUNT(DISTINCT sp.id)           AS productCount, " +
            "  COUNT(o.id)                     AS totalOrders, " +
            "  SUM(CASE WHEN o.status = 1 THEN 1 ELSE 0 END) AS paidOrders, " +
            "  SUM(CASE WHEN o.status = 2 THEN 1 ELSE 0 END) AS cancelledOrders, " +
            "  COALESCE(SUM(CASE WHEN o.status = 1 THEN o.actual_amount ELSE 0 END), 0) AS totalPaidAmount " +
            "FROM seckill_activity sa " +
            "LEFT JOIN seckill_product sp ON sp.activity_id = sa.id AND sp.merchant_id = #{merchantId} " +
            "LEFT JOIN `order` o ON o.seckill_product_id = sp.id AND o.merchant_id = #{merchantId} " +
            "WHERE sa.id = #{activityId} " +
            "GROUP BY sa.id, sa.name")
    SeckillActivityStatsVO selectSeckillStatsByActivity(@Param("merchantId") Long merchantId,
                                                         @Param("activityId") Long activityId);
}
