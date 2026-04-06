package com.eaxon.xtreme_pojo.dto;

import lombok.Data;

@Data
public class CategoryDTO {

    /** 分类名称 */
    private String name;

    /** 图标 URL */
    private String icon;

    /** 排序值，数字越小越靠前 */
    private Integer sortOrder;
}
