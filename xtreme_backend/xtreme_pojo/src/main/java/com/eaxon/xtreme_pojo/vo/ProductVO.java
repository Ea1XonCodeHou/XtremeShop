package com.eaxon.xtreme_pojo.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 公开商品列表 VO（用户首页浏览，不暴露商家内部字段）
 */
@Data
public class ProductVO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private String coverUrl;

    private Long categoryId;

    private Integer stock;

    private Integer soldCount;
}
