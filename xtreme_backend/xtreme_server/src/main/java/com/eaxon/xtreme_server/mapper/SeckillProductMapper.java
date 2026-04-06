package com.eaxon.xtreme_server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {

    /** 商家端：查看某活动内本商家的秒杀商品（JOIN product 获取商品名/封面/原价） */
    @Select("SELECT sp.id AS spId, sp.activity_id AS activityId, sp.product_id AS productId, " +
            "p.name AS productName, p.cover_url AS coverUrl, p.price AS originalPrice, " +
            "sp.seckill_price AS seckillPrice, sp.seckill_stock AS seckillStock, " +
            "sp.seckill_stock_init AS seckillStockInit, sp.limit_per_user AS limitPerUser " +
            "FROM seckill_product sp JOIN product p ON sp.product_id = p.id " +
            "WHERE sp.activity_id = #{activityId} AND p.merchant_id = #{merchantId} " +
            "ORDER BY sp.created_at DESC")
    List<SeckillProductVO> selectByActivityAndMerchant(@Param("activityId") Long activityId,
                                                       @Param("merchantId") Long merchantId);

    /** 用户端：获取所有进行中活动的秒杀商品（首页展示用） */
    @Select("SELECT sp.id AS spId, sp.activity_id AS activityId, sa.name AS activityName, " +
            "sp.product_id AS productId, p.name AS productName, p.cover_url AS coverUrl, " +
            "p.price AS originalPrice, sp.seckill_price AS seckillPrice, " +
            "sp.seckill_stock AS seckillStock, sp.seckill_stock_init AS seckillStockInit, " +
            "sp.limit_per_user AS limitPerUser, sa.end_time AS activityEndTime " +
            "FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "JOIN product p ON sp.product_id = p.id " +
            "WHERE sa.status = 1 AND p.is_on_sale = 1 " +
            "ORDER BY sa.end_time ASC")
    List<SeckillProductVO> selectActiveSeckillProducts();

    /** 统计某活动内本商家的秒杀商品数量 */
    @Select("SELECT COUNT(*) FROM seckill_product sp JOIN product p ON sp.product_id = p.id " +
            "WHERE sp.activity_id = #{activityId} AND p.merchant_id = #{merchantId}")
    int countByActivityAndMerchant(@Param("activityId") Long activityId,
                                    @Param("merchantId") Long merchantId);

    /** 用户端：根据 spId 查询单个秒杀商品详情（下单确认页用） */
    @Select("SELECT sp.id AS spId, sp.activity_id AS activityId, sa.name AS activityName, " +
            "sp.product_id AS productId, p.name AS productName, p.cover_url AS coverUrl, " +
            "p.price AS originalPrice, sp.seckill_price AS seckillPrice, " +
            "sp.seckill_stock AS seckillStock, sp.seckill_stock_init AS seckillStockInit, " +
            "sp.limit_per_user AS limitPerUser, sa.end_time AS activityEndTime " +
            "FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "JOIN product p ON sp.product_id = p.id " +
            "WHERE sp.id = #{spId} AND p.is_on_sale = 1")
    SeckillProductVO selectActiveById(@Param("spId") Long spId);

    /**
     * 异步落库后同步扣减 DB 库存，保证 DB 与 Redis 最终一致。
     * 限制条件 seckill_stock > 0 防止超扣（Redis Lua 已保证原子性，此处仅做安全托底）。
     */
    @Update("UPDATE seckill_product SET seckill_stock = seckill_stock - 1 WHERE id = #{spId} AND seckill_stock > 0")
    int decrementStock(@Param("spId") Long spId);

    /**
     * 取消订单时回补 DB 库存（+1），与 Redis INCR 配合保证最终一致性
     */
    @Update("UPDATE seckill_product SET seckill_stock = seckill_stock + 1 WHERE id = #{spId}")
    int incrementStock(@Param("spId") Long spId);

    /**
     * 查询即将开始（五分钟内）以及进行中的秒杀商品 spId，用于定时预热 Redis 库存
     */
    @Select("SELECT sp.id FROM seckill_product sp " +
            "JOIN seckill_activity sa ON sp.activity_id = sa.id " +
            "WHERE (sa.status = 1 OR (sa.status = 0 AND sa.start_time <= DATE_ADD(NOW(), INTERVAL 5 MINUTE))) " +
            "AND sa.end_time > NOW()")
    List<Long> selectSpIdsForWarmUp();
}
