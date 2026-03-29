package com.eaxon.xtreme_pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {

    private String name;

    /** 1=满减券 2=折扣券 */
    private Integer type;

    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    private Integer totalCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
