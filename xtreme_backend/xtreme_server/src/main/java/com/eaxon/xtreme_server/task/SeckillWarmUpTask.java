package com.eaxon.xtreme_server.task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eaxon.xtreme_pojo.vo.SeckillProductVO;
import com.eaxon.xtreme_server.mapper.SeckillActivityMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;
import com.eaxon.xtreme_server.service.OrderService;
import com.eaxon.xtreme_server.utils.CacheClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 秒杀活动库存预热定时任务
 * <p>
 * 每分钟执行一次，完成两件事：
 * <ol>
 *   <li>刷新活动状态（自动开始/结束活动，与手动查询时的 refreshStatus 逻辑相同）</li>
 *   <li>将即将开始（5 分钟内）及进行中活动的所有秒杀商品库存写入 Redis（SET NX，幂等）</li>
 * </ol>
 * 这样用户进入首页时，Redis 库存已预就绪，首单抢购无需等待 DB 查询。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillWarmUpTask {

    private static final String CACHE_PRODUCT_PREFIX      = "seckill:product:";
    private static final long   CACHE_PRODUCT_TTL_SECONDS = 60L;

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper  seckillProductMapper;
    private final OrderService          orderService;
    private final CacheClient           cacheClient;

    @Scheduled(fixedDelay = 60_000)
    public void warmUp() {
        // Step 1：刷新活动状态，确保 status 字段反映当前时间
        int activated = activityMapper.activateStarted();
        int deactivated = activityMapper.deactivateEnded();
        if (activated > 0 || deactivated > 0) {
            log.info("活动状态刷新 - 刚开始: {}, 刚结束: {}", activated, deactivated);
        }

        // Step 2：查询即将开始（5分钟内）+ 进行中的秒杀商品
        List<Long> spIds = seckillProductMapper.selectSpIdsForWarmUp();
        if (spIds.isEmpty()) {
            return;
        }

        int warmed = 0;
        for (Long spId : spIds) {
            try {
                // 预热 Redis 库存（SET NX，幂等）
                orderService.warmUpSeckillStock(spId);
                // 预热单品详情缓存（逻辑过期，防缓存击穿）
                SeckillProductVO vo = seckillProductMapper.selectActiveById(spId);
                if (vo != null) {
                    cacheClient.setWithLogicalExpiry(
                            CACHE_PRODUCT_PREFIX + spId, vo,
                            CACHE_PRODUCT_TTL_SECONDS, TimeUnit.SECONDS);
                }
                warmed++;
            } catch (Exception e) {
                log.warn("秒杀库存预热失败 - spId: {}, reason: {}", spId, e.getMessage());
            }
        }
        log.info("秒杀库存预热完成 - 共处理 {} 个商品", warmed);
    }
}
