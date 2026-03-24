-- 订单表
-- 秒杀下单后生成，关联秒杀商品、用户、优惠券
-- 分布式场景下：先写 Redis 预减库存，再异步落库到本表

CREATE TABLE IF NOT EXISTS `order` (
  `id`              BIGINT         NOT NULL AUTO_INCREMENT          COMMENT '订单ID',
  `order_no`        VARCHAR(32)    NOT NULL                         COMMENT '订单号（唯一，时间戳+随机数生成）',
  `user_id`         BIGINT         NOT NULL                         COMMENT '下单用户ID（关联 user.id）',
  `merchant_id`     BIGINT         NOT NULL                         COMMENT '商家ID（关联 merchant.id）',
  `product_id`      BIGINT         NOT NULL                         COMMENT '商品ID（关联 product.id）',
  `seckill_product_id` BIGINT                                       COMMENT '秒杀商品ID（关联 seckill_product.id，普通购买为 NULL）',
  `user_coupon_id`  BIGINT                                          COMMENT '使用的用户优惠券ID（关联 user_coupon.id，不使用为 NULL）',
  `quantity`        INT            NOT NULL DEFAULT 1               COMMENT '购买数量',
  `original_price`  DECIMAL(10, 2) NOT NULL                         COMMENT '商品原价（下单时快照，防止商家改价）',
  `seckill_price`   DECIMAL(10, 2) NOT NULL                         COMMENT '成交单价（秒杀价或原价）',
  `discount_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00            COMMENT '优惠券减免金额',
  `actual_amount`   DECIMAL(10, 2) NOT NULL                         COMMENT '实付总价 = seckill_price × quantity - discount_amount',
  `status`          TINYINT(1)     NOT NULL DEFAULT 0               COMMENT '状态：0=待支付 1=已支付 2=已取消 3=已退款',
  `created_at`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `updated_at`      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `pay_time`        DATETIME                                        COMMENT '支付时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user`    (`user_id`),
  KEY `idx_merchant`(`merchant_id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_status`  (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='订单表 - 秒杀/普通下单，含价格快照与优惠券核销';
