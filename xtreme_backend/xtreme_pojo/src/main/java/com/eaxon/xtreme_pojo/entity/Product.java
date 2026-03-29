package com.eaxon.xtreme_pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long categoryId;

    private String name;

    private String description;

    private String coverUrl;

    private BigDecimal price;

    private Integer stock;

    private Integer soldCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic(value = "1", delval = "0")
    private Integer isOnSale;
}
