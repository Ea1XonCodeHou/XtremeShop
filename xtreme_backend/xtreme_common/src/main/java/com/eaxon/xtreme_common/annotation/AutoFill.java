package com.eaxon.xtreme_common.annotation;

import com.eaxon.xtreme_common.enums.OperationType;

import java.lang.annotation.*;

/**
 * 自动填充时间字段注解
 * 用于 Mapper 的 insert/update 方法上
 * 由 AutoFillAspect 切面处理，自动填充 createdAt / updatedAt
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFill {
    OperationType value();
}
