package com.eaxon.xtreme_server.aspect;

import com.eaxon.xtreme_common.annotation.AutoFill;
import com.eaxon.xtreme_common.enums.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * AutoFill 切面：拦截带 @AutoFill 注解的 Mapper 方法，
 * 自动通过反射填充实体的 createdAt / updatedAt 字段
 *
 * 约定：方法的第一个参数为待填充的实体对象
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Before("@annotation(com.eaxon.xtreme_common.annotation.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws Exception {
        log.debug("AutoFillAspect triggered: {}", joinPoint.getSignature());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationType operationType = signature.getMethod()
                .getAnnotation(AutoFill.class)
                .value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();

        if (operationType == OperationType.INSERT) {
            setField(entity, "setCreatedAt", now);
            setField(entity, "setUpdatedAt", now);
        } else if (operationType == OperationType.UPDATE) {
            setField(entity, "setUpdatedAt", now);
        }
    }

    private void setField(Object entity, String methodName, LocalDateTime value) {
        try {
            Method method = entity.getClass().getMethod(methodName, LocalDateTime.class);
            method.invoke(entity, value);
        } catch (NoSuchMethodException e) {
            log.warn("AutoFillAspect: {} 上未找到方法 {}，跳过填充", entity.getClass().getSimpleName(), methodName);
        } catch (Exception e) {
            log.error("AutoFillAspect: 填充字段失败 - {}", e.getMessage());
        }
    }
}
