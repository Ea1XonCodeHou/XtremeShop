-- ============================================================
-- XtremeShop 数据库初始化脚本
-- 由 docker-compose 在 MySQL 容器首次启动时自动执行
-- 执行顺序：user → merchant → category → product → seckill → coupon → order
-- 注意：order.sql 已含 receiver/phone/address 字段，
--       order_add_shipping_fields.sql（迁移脚本）不在此引入
-- ============================================================

-- ────────────────── 用户表 ──────────────────
CREATE TABLE IF NOT EXISTS `user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键，自增）',
  `phone`      VARCHAR(20)  NOT NULL UNIQUE         COMMENT '手机号（登录凭证，唯一索引确保不重复注册）',
  `username`   VARCHAR(50)                          COMMENT '用户名（可选，支持用户自定义昵称）',
  `password`   VARCHAR(255) NOT NULL                COMMENT '密码哈希值（MD5+盐值加密存储，不存储明文）',
  `salt`       VARCHAR(32)  NOT NULL                COMMENT '密码盐值（随机生成，提升密码安全性，防彩虹表攻击）',
  `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '创建时间（用户注册时自动填充）',
  `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（密码修改等操作时更新）',
  `is_active`  TINYINT(1)   DEFAULT 1               COMMENT '账户状态（1=活跃，0=禁用，预留字段）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户表 - 支持分布式session登录、加盐加密密码存储';


-- ────────────────── 商家表 ──────────────────
CREATE TABLE IF NOT EXISTS `merchant` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT         COMMENT '商家ID',
  `name`        VARCHAR(64)  NOT NULL                        COMMENT '店铺名称',
  `phone`       VARCHAR(20)  NOT NULL                        COMMENT '登录手机号（唯一）',
  `password`    VARCHAR(255) NOT NULL                        COMMENT 'MD5+盐值加密密码',
  `salt`        VARCHAR(32)  NOT NULL                        COMMENT '密码盐值',
  `logo_url`    VARCHAR(512)                                 COMMENT '店铺 Logo（阿里云 OSS URL）',
  `description` VARCHAR(256)                                 COMMENT '店铺简介',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_active`   TINYINT(1)   NOT NULL DEFAULT 1              COMMENT '账户状态 1=正常 0=禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=2000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='商家表 - 独立登录体系，与普通用户隔离';


-- ────────────────── 商品分类表 ──────────────────
CREATE TABLE IF NOT EXISTS `category` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`       VARCHAR(32) NOT NULL                COMMENT '分类名称（如：手机数码、服饰鞋包）',
  `icon`       VARCHAR(64)                         COMMENT 'Material Icons 图标名，前端直接渲染',
  `sort_order` INT         NOT NULL DEFAULT 0      COMMENT '排序权重，越小越靠前',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='商品分类表';

-- 初始化分类数据（对应前端 Home 页侧边栏）
INSERT INTO `category` (`name`, `icon`, `sort_order`) VALUES
('手机数码', 'smartphone',     1),
('家用电器', 'kitchen',        2),
('电脑办公', 'computer',       3),
('美妆护肤', 'face',           4),
('食品饮料', 'fastfood',       5),
('男装女装', 'checkroom',      6),
('运动户外', 'directions_run', 7);


-- ────────────────── 商品表 ──────────────────
CREATE TABLE IF NOT EXISTS `product` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT         NOT NULL                COMMENT '所属商家ID（关联 merchant.id）',
  `category_id` BIGINT         NOT NULL                COMMENT '分类ID（关联 category.id）',
  `name`        VARCHAR(128)   NOT NULL                COMMENT '商品名称',
  `description` VARCHAR(512)                           COMMENT '商品简介',
  `cover_url`   VARCHAR(512)                           COMMENT '封面缩略图（阿里云 OSS URL）',
  `price`       DECIMAL(10, 2) NOT NULL                COMMENT '商品原价（单位：元）',
  `stock`       INT            NOT NULL DEFAULT 0      COMMENT '普通库存（非秒杀）',
  `sold_count`  INT            NOT NULL DEFAULT 0      COMMENT '累计销量（展示用）',
  `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '发布时间',
  `updated_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_on_sale`  TINYINT(1)     NOT NULL DEFAULT 1      COMMENT '上架状态 1=上架 0=下架',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='商品表 - 商家发布，支持分类关联与 OSS 图片';


-- ────────────────── 秒杀活动表 ──────────────────
CREATE TABLE IF NOT EXISTS `seckill_activity` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name`       VARCHAR(64) NOT NULL                COMMENT '活动名称（如：618 限时秒杀）',
  `start_time` DATETIME    NOT NULL                COMMENT '活动开始时间',
  `end_time`   DATETIME    NOT NULL                COMMENT '活动结束时间',
  `status`     TINYINT(1)  NOT NULL DEFAULT 0      COMMENT '状态：0=未开始 1=进行中 2=已结束',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='秒杀活动表';


-- ────────────────── 秒杀商品表 ──────────────────
CREATE TABLE IF NOT EXISTS `seckill_product` (
  `id`                 BIGINT         NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `activity_id`        BIGINT         NOT NULL                COMMENT '所属活动ID（关联 seckill_activity.id）',
  `product_id`         BIGINT         NOT NULL                COMMENT '商品ID（关联 product.id）',
  `seckill_price`      DECIMAL(10, 2) NOT NULL                COMMENT '秒杀价（单位：元）',
  `seckill_stock`      INT            NOT NULL DEFAULT 0      COMMENT '剩余秒杀库存（Redis 同步）',
  `seckill_stock_init` INT            NOT NULL DEFAULT 0      COMMENT '初始秒杀库存（用于复盘）',
  `limit_per_user`     INT            NOT NULL DEFAULT 1      COMMENT '每人限购数量',
  `created_at`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_product` (`activity_id`, `product_id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_product`  (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='秒杀商品表 - 活动内商品的秒杀价与库存';


-- ────────────────── 优惠券表 ──────────────────
CREATE TABLE IF NOT EXISTS `coupon` (
  `id`               BIGINT         NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `merchant_id`      BIGINT         NOT NULL                COMMENT '发券商家ID（关联 merchant.id）',
  `name`             VARCHAR(64)    NOT NULL                COMMENT '券名称（如：满100减20）',
  `type`             TINYINT(1)     NOT NULL DEFAULT 1      COMMENT '类型：1=满减券 2=折扣券',
  `discount_value`   DECIMAL(10, 2) NOT NULL                COMMENT '满减金额 或 折扣率（折扣券如：0.8 表示8折）',
  `min_order_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0      COMMENT '最低消费门槛（0=无门槛）',
  `total_count`      INT            NOT NULL DEFAULT 0      COMMENT '发行总量（0=不限量）',
  `used_count`       INT            NOT NULL DEFAULT 0      COMMENT '已使用数量',
  `get_count`        INT            NOT NULL DEFAULT 0      COMMENT '已领取数量',
  `start_time`       DATETIME       NOT NULL                COMMENT '有效期开始',
  `end_time`         DATETIME       NOT NULL                COMMENT '有效期结束',
  `created_at`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_active`        TINYINT(1)     NOT NULL DEFAULT 1      COMMENT '状态 1=有效 0=停用',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='优惠券表 - 商家发布，支持秒杀活动专属券';


-- ────────────────── 用户优惠券表 ──────────────────
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id`        BIGINT     NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`   BIGINT     NOT NULL                COMMENT '用户ID（关联 user.id）',
  `coupon_id` BIGINT     NOT NULL                COMMENT '优惠券ID（关联 coupon.id）',
  `status`    TINYINT(1) NOT NULL DEFAULT 0      COMMENT '状态：0=未使用 1=已使用 2=已过期',
  `get_time`  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time`  DATETIME                           COMMENT '使用时间（核销时更新）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
  KEY `idx_user`   (`user_id`),
  KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户优惠券表 - 记录领取与使用状态';


-- ────────────────── 订单表 ──────────────────
-- 已含 receiver/phone/address 字段（order_add_shipping_fields.sql 为历史迁移脚本，此处无需再 ALTER）
CREATE TABLE IF NOT EXISTS `order` (
  `id`                 BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`           VARCHAR(32)    NOT NULL                COMMENT '订单号（唯一，时间戳+随机数生成）',
  `user_id`            BIGINT         NOT NULL                COMMENT '下单用户ID（关联 user.id）',
  `merchant_id`        BIGINT         NOT NULL                COMMENT '商家ID（关联 merchant.id）',
  `product_id`         BIGINT         NOT NULL                COMMENT '商品ID（关联 product.id）',
  `seckill_product_id` BIGINT                                 COMMENT '秒杀商品ID（关联 seckill_product.id，普通购买为 NULL）',
  `user_coupon_id`     BIGINT                                 COMMENT '使用的用户优惠券ID（关联 user_coupon.id，不使用为 NULL）',
  `quantity`           INT            NOT NULL DEFAULT 1      COMMENT '购买数量',
  `original_price`     DECIMAL(10, 2) NOT NULL                COMMENT '商品原价（下单时快照，防止商家改价）',
  `seckill_price`      DECIMAL(10, 2) NOT NULL                COMMENT '成交单价（秒杀价或原价）',
  `discount_amount`    DECIMAL(10, 2) NOT NULL DEFAULT 0.00   COMMENT '优惠券减免金额',
  `actual_amount`      DECIMAL(10, 2) NOT NULL                COMMENT '实付总价 = seckill_price × quantity - discount_amount',
  `receiver`           VARCHAR(32)                            COMMENT '收货人姓名',
  `phone`              VARCHAR(16)                            COMMENT '收货手机号',
  `address`            VARCHAR(200)                           COMMENT '收货地址',
  `status`             TINYINT(1)     NOT NULL DEFAULT 0      COMMENT '状态：0=待支付 1=已支付 2=已取消 3=已退款',
  `created_at`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP                   COMMENT '下单时间',
  `updated_at`         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `pay_time`           DATETIME                               COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user`     (`user_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_product`  (`product_id`),
  KEY `idx_status`   (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='订单表 - 秒杀/普通下单，含价格快照与优惠券核销';
