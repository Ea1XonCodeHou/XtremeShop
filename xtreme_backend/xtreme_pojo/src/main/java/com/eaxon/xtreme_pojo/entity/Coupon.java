package com.eaxon.xtreme_pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private String name;

    /** 1=满减券 2=折扣券 */
    private Integer type;

    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;

    private Integer totalCount;

    private Integer usedCount;

    private Integer getCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer isActive;
}
