-- 商家表
-- 商家独立登录，主页提供「我是商家」入口，走本表查询

CREATE TABLE IF NOT EXISTS `merchant` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT         COMMENT '商家ID',
  `name`        VARCHAR(64)  NOT NULL                        COMMENT '店铺名称',
  `phone`       VARCHAR(20)  NOT NULL                        COMMENT '登录手机号（唯一）',
  `password`    VARCHAR(255) NOT NULL                        COMMENT 'MD5+盐值加密密码',
  `salt`        VARCHAR(32)  NOT NULL                        COMMENT '密码盐值',
  `logo_url`    VARCHAR(512)                                 COMMENT '店铺 Logo（阿里云 OSS URL）',
  `description` VARCHAR(256)                                 COMMENT '店铺简介',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_active`   TINYINT(1)   NOT NULL DEFAULT 1              COMMENT '账户状态 1=正常 0=禁用',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=2000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='商家表 - 独立登录体系，与普通用户隔离';
