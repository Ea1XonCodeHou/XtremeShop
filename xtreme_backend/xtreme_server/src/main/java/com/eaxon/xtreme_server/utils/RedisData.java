package com.eaxon.xtreme_server.utils;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 用于缓存击穿「逻辑过期」方案的 Redis 数据包装类
 * <p>
 * Redis Key 本身不设置 TTL（永不自动删除），由业务代码判断 {@code expireTime}
 * 是否已过期，过期则异步重建数据、同步返回旧值，避免缓存击穿。
 *
 * @param <T> 实际业务数据类型
 */
@Data
public class RedisData<T> {

    /** 逻辑过期时间：超过此时间则认为数据需要重建 */
    private LocalDateTime expireTime;

    /** 业务数据，JSON 反序列化时请传入正确的目标类型 */
    private T data;
}
