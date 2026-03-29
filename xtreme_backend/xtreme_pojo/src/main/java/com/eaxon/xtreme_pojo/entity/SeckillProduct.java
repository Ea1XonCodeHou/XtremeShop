package com.eaxon.xtreme_pojo.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("seckill_product")
public class SeckillProduct implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long productId;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer seckillStockInit;

    private Integer limitPerUser;

    private LocalDateTime createdAt;
}
