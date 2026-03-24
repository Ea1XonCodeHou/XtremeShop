-- 商品分类表
-- 用于主页左侧分类导航 + 商家发布商品时选择分类

CREATE TABLE IF NOT EXISTS `category` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT          COMMENT '分类ID',
  `name`       VARCHAR(32) NOT NULL                         COMMENT '分类名称（如：手机数码、服饰鞋包）',
  `icon`       VARCHAR(64)                                  COMMENT 'Material Icons 图标名，前端直接渲染',
  `sort_order` INT         NOT NULL DEFAULT 0               COMMENT '排序权重，越小越靠前',
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
