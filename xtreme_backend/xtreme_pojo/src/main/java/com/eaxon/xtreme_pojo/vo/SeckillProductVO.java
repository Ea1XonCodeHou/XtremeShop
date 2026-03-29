package com.eaxon.xtreme_pojo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class SeckillProductVO {

    private Long spId;

    private Long activityId;

    private String activityName;

    private Long productId;

    private String productName;

    private String coverUrl;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer seckillStockInit;

    private Integer limitPerUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEndTime;
}
