package com.eaxon.xtreme_pojo.dto;

import lombok.Data;

/**
 * 用户秒杀下单入参 DTO
 * 前端提交的字段：选中的秒杀商品 + 简化收货信息
 */
@Data
public class SeckillOrderDTO {

    /** 秒杀商品 ID（seckill_product.id） */
    private Long seckillProductId;

    /** 活动 ID（seckill_activity.id），与 seckillProductId 配合用于 Redis 限购 Set key */
    private Long activityId;

    /** 收货人姓名 */
    private String receiver;

    /** 收货手机号 */
    private String phone;

    /** 收货地址 */
    private String address;
}
