-- 秒杀活动表 + 秒杀商品表
-- seckill_activity：一次秒杀活动（如「618 限时秒杀」）
-- seckill_product：活动内的具体商品，设置秒杀价和秒杀库存

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT          COMMENT '活动ID',
  `name`       VARCHAR(64) NOT NULL                         COMMENT '活动名称（如：618 限时秒杀）',
  `start_time` DATETIME    NOT NULL                         COMMENT '活动开始时间',
  `end_time`   DATETIME    NOT NULL                         COMMENT '活动结束时间',
  `status`     TINYINT(1)  NOT NULL DEFAULT 0               COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='秒杀活动表';


-- 秒杀商品表（活动与商品的关联，设置秒杀价/库存）
CREATE TABLE IF NOT EXISTS `seckill_product` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT          COMMENT '秒杀商品ID',
  `activity_id`      BIGINT         NOT NULL                         COMMENT '所属活动ID（关联 seckill_activity.id）',
  `product_id`       BIGINT         NOT NULL                         COMMENT '商品ID（关联 product.id）',
  `seckill_price`    DECIMAL(10, 2) NOT NULL                         COMMENT '秒杀价（单位：元）',
  `seckill_stock`    INT            NOT NULL DEFAULT 0               COMMENT '剩余秒杀库存（Redis 同步）',
  `seckill_stock_init` INT          NOT NULL DEFAULT 0               COMMENT '初始秒杀库存（用于复盘）',
  `limit_per_user`   INT            NOT NULL DEFAULT 1               COMMENT '每人限购数量',
  `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_product` (`activity_id`, `product_id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_product`  (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='秒杀商品表 - 活动内商品的秒杀价与库存';
