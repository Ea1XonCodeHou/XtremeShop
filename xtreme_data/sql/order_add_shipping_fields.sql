-- 订单表添加收货信息字段
-- 执行时间：2026-03-31
-- 说明：为支持秒杀下单确认页填写收货信息

ALTER TABLE `order`
ADD COLUMN `receiver` VARCHAR(50) NULL COMMENT '收货人姓名' AFTER `actual_amount`,
ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '收货手机号' AFTER `receiver`,
ADD COLUMN `address` VARCHAR(255) NULL COMMENT '收货地址' AFTER `phone`;
