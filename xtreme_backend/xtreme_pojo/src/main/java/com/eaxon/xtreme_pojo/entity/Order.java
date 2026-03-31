package com.eaxon.xtreme_pojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 订单实体（对应 `order` 表）
 * - 秒杀下单异步落库的目标表
 * - seckill_product_id 不为 NULL 即为秒杀订单
 */
@Data
@TableName("`order`") // order 是 MySQL 保留字，需加反引号
public class Order implements Serializable {

    /** 订单 ID（AUTO_INCREMENT，从 100000 起步） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 全局唯一订单号（RedisIdWorker 生成） */
    private String orderNo;

    /** 下单用户 ID */
    private Long userId;

    /** 商家 ID */
    private Long merchantId;

    /** 商品 ID */
    private Long productId;

    /** 秒杀商品 ID（NULL = 普通购买） */
    private Long seckillProductId;

    /** 使用的用户优惠券 ID（NULL = 未使用） */
    private Long userCouponId;

    /** 购买数量（秒杀固定为 1） */
    private Integer quantity;

    /** 商品原价快照（下单时记录，防止商家改价后不一致） */
    private BigDecimal originalPrice;

    /** 成交单价（秒杀订单取秒杀价） */
    private BigDecimal seckillPrice;

    /** 优惠券减免金额 */
    private BigDecimal discountAmount;

    /** 实付总价 = seckill_price × quantity - discount_amount */
    private BigDecimal actualAmount;

    /** 收货人姓名 */
    private String receiver;

    /** 收货手机号 */
    private String phone;

    /** 收货地址 */
    private String address;

    /**
     * 订单状态
     * 0 = 待支付
     * 1 = 已支付
     * 2 = 已取消
     * 3 = 已退款
     */
    private Integer status;

    /** 下单时间 */
    private LocalDateTime createdAt;

    /** 状态更新时间 */
    private LocalDateTime updatedAt;

    /** 支付时间（未支付为 NULL） */
    private LocalDateTime payTime;
}
