package com.eaxon.xtreme_pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Long categoryId;

    private String description;

    private String coverUrl;
}
