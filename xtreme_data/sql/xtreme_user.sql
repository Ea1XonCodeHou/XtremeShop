-- XtremeShop 用户表建表SQL
-- 表名：user
-- 功能：存储用户基础信息，支持分布式session和密码安全存储

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键，自增）',
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号（登录凭证，唯一索引确保不重复注册）',
  `username` VARCHAR(50) COMMENT '用户名（可选，支持用户自定义昵称）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码哈希值（MD5+盐值加密存储，不存储明文）',
  `salt` VARCHAR(32) NOT NULL COMMENT '密码盐值（随机生成，提升密码安全性，防彩虹表攻击）',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（用户注册时自动填充）',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（密码修改等操作时更新）',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '账户状态（1=活跃，0=禁用，预留字段）',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表 - 支持分布式session登录、加盐加密密码存储';
