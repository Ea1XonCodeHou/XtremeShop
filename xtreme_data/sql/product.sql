-- 商品表
-- 由商家发布，关联商家与分类；cover_url 存阿里云 OSS 地址

CREATE TABLE IF NOT EXISTS `product` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT          COMMENT '商品ID',
  `merchant_id` BIGINT         NOT NULL                         COMMENT '所属商家ID（关联 merchant.id）',
  `category_id` BIGINT         NOT NULL                         COMMENT '分类ID（关联 category.id）',
  `name`        VARCHAR(128)   NOT NULL                         COMMENT '商品名称',
  `description` VARCHAR(512)                                    COMMENT '商品简介',
  `cover_url`   VARCHAR(512)                                    COMMENT '封面缩略图（阿里云 OSS URL）',
  `price`       DECIMAL(10, 2) NOT NULL                         COMMENT '商品原价（单位：元）',
  `stock`       INT            NOT NULL DEFAULT 0               COMMENT '普通库存（非秒杀）',
  `sold_count`  INT            NOT NULL DEFAULT 0               COMMENT '累计销量（展示用）',
  `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_on_sale`  TINYINT(1)     NOT NULL DEFAULT 1               COMMENT '上架状态 1=上架 0=下架',

  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='商品表 - 商家发布，支持分类关联与 OSS 图片';
