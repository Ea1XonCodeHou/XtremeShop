package com.eaxon.xtreme_common.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 基于 Redis 的全局唯一 ID 生成器（参考雪花算法，无机器码）
 *
 * ID 结构（64 bit）：
 * ┌─────────┬──────────────────────────────────┬────────────────────────────────────┐
 * │ 符号位(1)│       时间戳-秒(31 bit)           │      Redis 自增序列(32 bit)         │
 * └─────────┴──────────────────────────────────┴────────────────────────────────────┘
 *
 * - 时间戳：当前秒数 - 基准时间（2024-01-01 00:00:00），可用约 68 年
 * - 序列号：Redis INCR，按业务前缀独立计数，每天重置 key（防止无限增长）
 *
 * 使用示例：
 *   long orderId  = redisIdWorker.nextId("order");
 *   long couponId = redisIdWorker.nextId("coupon");
 */

@Component
public class RedisIdWorker {

    /** 基准时间戳（2024-01-01 00:00:00 UTC+8 的秒数） */
    private static final long BEGIN_TIMESTAMP = 1704067200L;

    /** 序列号占用的位数 */
    private static final int SEQUENCE_BITS = 32;

    private final StringRedisTemplate redisTemplate;

    public RedisIdWorker(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成全局唯一 ID
     *
     * @param bizPrefix 业务前缀，如 "order"、"coupon"、"seckill"
     * @return 64 bit 唯一 ID
     */
    public long nextId(String bizPrefix) {
        // 1. 时间戳部分（当前秒 - 基准秒）
        long nowSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        // 2. 序列号部分：Redis INCR，key 按天隔离（防止单 key 无限增长）
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = "id:" + bizPrefix + ":" + date;
        long sequence = redisTemplate.opsForValue().increment(redisKey);

        // 3. 拼接：时间戳左移 32 位 | 序列号
        return (timestamp << SEQUENCE_BITS) | sequence;
    }
}
