package com.eaxon.xtreme_server.utils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 缓存工具类
 *
 * <h3>封装的两种缓存防护方案</h3>
 *
 * <h4>1. 缓存穿透 —— 空值缓存（{@link #getWithPassThrough}）</h4>
 * <p>当 DB 也查不到数据时，向 Redis 写入一个 {@code ""} 空串（TTL 2 分钟），
 * 避免恶意请求每次都打穿到 DB。命中空串时直接返回 {@code null}。</p>
 *
 * <h4>2. 缓存击穿 —— 逻辑过期（{@link #getWithLogicalExpiry}）</h4>
 * <p>热点 Key 不设置真实 TTL，而是在 Value 中额外存储 {@code expireTime}。
 * 查询时若逻辑过期，由异步线程重建数据（使用简单互斥锁防止并发重建），
 * 重建完成前仍返回旧数据，保证高可用。</p>
 *
 * <h4>3. 缓存雪崩（不在本工具处理）</h4>
 * <p>本项目并发量有限，可在调用 {@link #set} 时在 TTL 上加随机抖动来规避，
 * 无需集中处理。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {

    /** 缓存穿透时写入的空值占位符 */
    private static final String NULL_VALUE = "";
    /** 空值 TTL（分钟）：避免占用 Redis 过久 */
    private static final long NULL_TTL_MINUTES = 2L;
    /** 互斥锁 Key 前缀 */
    private static final String LOCK_PREFIX = "lock:";

    /** 异步重建线程池（仅用于逻辑过期方案） */
    private static final ExecutorService REBUILD_EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
    }

    private final StringRedisTemplate redisTemplate;

    // ------------------------------------------------------------------ //
    //  基础写入
    // ------------------------------------------------------------------ //

    /** 向 Redis 写入任意对象（序列化为 JSON），指定过期时间 */
    public void set(String key, Object value, long time, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, MAPPER.writeValueAsString(value), time, unit);
        } catch (Exception e) {
            log.error("CacheClient set 失败 - key: {}", key, e);
        }
    }

    /**
     * 向 Redis 写入逻辑过期数据（不设置真实 TTL，key 永久存在）
     * 数据被包装为 {@link RedisData}（含 expireTime 字段）
     */
    public void setWithLogicalExpiry(String key, Object value, long time, TimeUnit unit) {
        RedisData<Object> data = new RedisData<>();
        data.setData(value);
        data.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        try {
            redisTemplate.opsForValue().set(key, MAPPER.writeValueAsString(data));
        } catch (Exception e) {
            log.error("CacheClient setWithLogicalExpiry 失败 - key: {}", key, e);
        }
    }

    // ------------------------------------------------------------------ //
    //  缓存穿透：空值缓存
    // ------------------------------------------------------------------ //

    /**
     * 查询缓存，解决缓存穿透（空值缓存方案）。
     *
     * <pre>
     * 流程：
     *   1. 命中缓存 & 非空串  → 反序列化返回
     *   2. 命中缓存 & 空串    → 返回 null（DB 中确实无此数据）
     *   3. 未命中             → 调用 dbFallback 查 DB
     *      a. DB 无数据       → 写入空串（TTL 2min），返回 null
     *      b. DB 有数据       → 写入缓存，返回数据
     * </pre>
     *
     * @param keyPrefix  缓存 Key 前缀
     * @param id         数据 ID（追加到 keyPrefix 后组成完整 Key）
     * @param type       返回值类型的 Class
     * @param dbFallback 查询 DB 的函数，参数类型与 id 一致
     * @param time       正常缓存过期时间
     * @param unit       过期时间单位
     * @param <T>        业务数据类型
     * @param <ID>       ID 类型
     * @return 业务数据，DB 无数据时返回 null
     */
    public <T, ID> T getWithPassThrough(
            String keyPrefix, ID id, Class<T> type,
            Function<ID, T> dbFallback, long time, TimeUnit unit) {

        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);

        // 命中缓存
        if (json != null) {
            if (json.isEmpty()) {
                // 缓存穿透：空值占位
                return null;
            }
            return deserialize(json, type);
        }

        // 未命中 → 查 DB
        T value = dbFallback.apply(id);
        if (value == null) {
            // DB 也无数据：写空值防止穿透
            redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_TTL_MINUTES, TimeUnit.MINUTES);
            return null;
        }

        set(key, value, time, unit);
        return value;
    }

    // ------------------------------------------------------------------ //
    //  缓存击穿：逻辑过期
    // ------------------------------------------------------------------ //

    /**
     * 查询缓存，解决缓存击穿（逻辑过期方案）。
     *
     * <pre>
     * 前提：热点数据已通过 {@link #setWithLogicalExpiry} 预加载到 Redis。
     * 流程：
     *   1. Key 不存在          → 视为非热点数据，直接返回 null（调用方负责降级）
     *   2. 逻辑未过期          → 直接返回已缓存数据
     *   3. 逻辑过期            → 尝试获取互斥锁
     *      a. 抢到锁            → 异步线程重建 → 返回旧数据
     *      b. 未抢到锁          → 直接返回旧数据（其他线程正在重建）
     * </pre>
     *
     * @param keyPrefix  缓存 Key 前缀
     * @param id         数据 ID
     * @param type       返回值类型的 Class
     * @param dbFallback 查询 DB 的函数
     * @param time       逻辑过期时长
     * @param unit       时长单位
     */
    public <T, ID> T getWithLogicalExpiry(
            String keyPrefix, ID id, Class<T> type,
            Function<ID, T> dbFallback, long time, TimeUnit unit) {

        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key);

        // Key 不存在（未预热）
        if (json == null || json.isEmpty()) {
            return null;
        }

        // 反序列化 RedisData 包装
        RedisData<?> redisData = deserialize(json, RedisData.class);
        if (redisData == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        T cachedValue = deserialize(MAPPER.valueToTree(redisData.getData()).toString(), type);

        // 逻辑未过期 → 直接返回
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return cachedValue;
        }

        // 逻辑过期 → 尝试互斥锁触发异步重建
        String lockKey = LOCK_PREFIX + key;
        boolean locked = tryLock(lockKey);
        if (locked) {
            // 拿到锁：异步重建
            final T stale = cachedValue;
            REBUILD_EXECUTOR.submit(() -> {
                try {
                    T fresh = dbFallback.apply(id);
                    if (fresh != null) {
                        setWithLogicalExpiry(key, fresh, time, unit);
                    }
                } catch (Exception e) {
                    log.error("CacheClient 逻辑过期重建失败 - key: {}", key, e);
                } finally {
                    releaseLock(lockKey);
                }
            });
        }

        // 无论是否拿到锁，先返回旧数据（保证可用性）
        return cachedValue;
    }

    // ------------------------------------------------------------------ //
    //  互斥锁辅助
    // ------------------------------------------------------------------ //

    /** 尝试获取互斥锁（SET NX EX 10s），返回是否成功 */
    private boolean tryLock(String lockKey) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

    /** 释放互斥锁 */
    private void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    // ------------------------------------------------------------------ //
    //  JSON 工具
    // ------------------------------------------------------------------ //

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            log.error("CacheClient JSON 反序列化失败 - type: {}", type.getSimpleName(), e);
            return null;
        }
    }
}
