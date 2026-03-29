package com.eaxon.xtreme_pojo.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SeckillProductDTO {

    private Long productId;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer limitPerUser;
}
