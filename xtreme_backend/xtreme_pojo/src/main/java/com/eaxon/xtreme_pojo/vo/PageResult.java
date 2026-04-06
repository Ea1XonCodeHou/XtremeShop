package com.eaxon.xtreme_pojo.vo;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用分页响应包装 VO
 * <p>
 * 前端统一以 { list: [...], total: N } 格式消费分页数据。
 * 使用泛型确保类型安全，替代 Map&lt;String, Object&gt; 手工拼装。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据列表 */
    private List<T> list;

    /** 总记录数（用于前端分页总数展示） */
    private long total;

    public static <T> PageResult<T> of(List<T> list, long total) {
        return new PageResult<>(list, total);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L);
    }
}
