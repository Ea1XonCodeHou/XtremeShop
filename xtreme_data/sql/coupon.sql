-- 优惠券表 + 用户优惠券表
-- coupon：商家发布的优惠券（满减 / 折扣），可作为秒杀活动专属券
-- user_coupon：用户抢到的券，记录领取/使用状态

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT          COMMENT '优惠券ID',
  `merchant_id`      BIGINT         NOT NULL                         COMMENT '发券商家ID（关联 merchant.id）',
  `name`             VARCHAR(64)    NOT NULL                         COMMENT '券名称（如：满100减20）',
  `type`             TINYINT(1)     NOT NULL DEFAULT 1               COMMENT '类型：1=满减券 2=折扣券',
  `discount_value`   DECIMAL(10, 2) NOT NULL                         COMMENT '满减金额 或 折扣率（折扣券如：0.8 表示8折）',
  `min_order_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0               COMMENT '最低消费门槛（0=无门槛）',
  `total_count`      INT            NOT NULL DEFAULT 0               COMMENT '发行总量（0=不限量）',
  `used_count`       INT            NOT NULL DEFAULT 0               COMMENT '已使用数量',
  `get_count`        INT            NOT NULL DEFAULT 0               COMMENT '已领取数量',
  `start_time`       DATETIME       NOT NULL                         COMMENT '有效期开始',
  `end_time`         DATETIME       NOT NULL                         COMMENT '有效期结束',
  `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_active`        TINYINT(1)     NOT NULL DEFAULT 1               COMMENT '状态 1=有效 0=停用',

  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='优惠券表 - 商家发布，支持秒杀活动专属券';


-- 用户优惠券表（用户抢券 / 使用记录）
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id`         BIGINT     NOT NULL AUTO_INCREMENT          COMMENT '记录ID',
  `user_id`    BIGINT     NOT NULL                         COMMENT '用户ID（关联 user.id）',
  `coupon_id`  BIGINT     NOT NULL                         COMMENT '优惠券ID（关联 coupon.id）',
  `status`     TINYINT(1) NOT NULL DEFAULT 0               COMMENT '状态：0=未使用 1=已使用 2=已过期',
  `get_time`   DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time`   DATETIME                                    COMMENT '使用时间（核销时更新）',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),   -- 每人每张券只能领一次
  KEY `idx_user`   (`user_id`),
  KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户优惠券表 - 记录领取与使用状态';
