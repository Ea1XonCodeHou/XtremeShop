package com.eaxon.xtreme_server.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eaxon.xtreme_pojo.dto.SeckillActivityDTO;
import com.eaxon.xtreme_pojo.dto.SeckillProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.entity.SeckillActivity;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_pojo.vo.SeckillActivityStatsVO;
import com.eaxon.xtreme_pojo.vo.SeckillActivityVO;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.mapper.SeckillActivityMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;
import com.eaxon.xtreme_server.service.SeckillService;
import com.eaxon.xtreme_server.utils.CacheClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    /** 秒杀商品列表缓存 Key */
    private static final String CACHE_ACTIVE_PRODUCTS     = "seckill:active_products";
    /** 秒杀商品详情缓存 Key 前缀 */
    private static final String CACHE_PRODUCT_PREFIX      = "seckill:product:";
    /** 列表缓存 TTL：30 秒（上新频率较高，短 TTL 保证数据相对新鲜） */
    private static final long   CACHE_ACTIVE_TTL_SECONDS  = 30L;
    /** 单品缓存逻辑过期时长：60 秒 */
    private static final long   CACHE_PRODUCT_TTL_SECONDS = 60L;

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper  seckillProductMapper;
    private final ProductMapper         productMapper;
    private final OrderMapper           orderMapper;
    private final CacheClient           cacheClient;

    @Override
    public void createActivity(SeckillActivityDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("活动名称不能为空");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new RuntimeException("开始时间和结束时间不能为空");
        }
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }

        SeckillActivity activity = new SeckillActivity();
        activity.setName(dto.getName());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setCreatedAt(LocalDateTime.now());

        // 根据当前时间动态计算初始状态
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dto.getStartTime())) {
            activity.setStatus(0);
        } else if (now.isBefore(dto.getEndTime())) {
            activity.setStatus(1);
        } else {
            activity.setStatus(2);
        }

        activityMapper.insert(activity);
        log.info("秒杀活动创建成功 - id: {}, name: {}, status: {}", activity.getId(), activity.getName(), activity.getStatus());
    }

    @Override
    public List<SeckillActivityVO> listActivities(Long merchantId) {
        // 先刷新所有活动的状态（基于当前时间）
        refreshStatus();

        List<SeckillActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .orderByDesc(SeckillActivity::getCreatedAt)
        );

        return activities.stream().map(a -> {
            SeckillActivityVO vo = new SeckillActivityVO();
            vo.setId(a.getId());
            vo.setName(a.getName());
            vo.setStartTime(a.getStartTime());
            vo.setEndTime(a.getEndTime());
            vo.setStatus(a.getStatus());
            vo.setStatusText(statusText(a.getStatus()));
            vo.setCreatedAt(a.getCreatedAt());
            vo.setProductCount(seckillProductMapper.countByActivityAndMerchant(a.getId(), merchantId));
            return vo;
        }).toList();
    }

    @Override
    public void deleteActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }

        // 检查是否还有关联的秒杀商品
        Long count = seckillProductMapper.selectCount(
                new LambdaQueryWrapper<SeckillProduct>()
                        .eq(SeckillProduct::getActivityId, activityId)
        );
        if (count > 0) {
            throw new RuntimeException("该活动下还有秒杀商品，请先移除所有商品再删除活动");
        }

        activityMapper.deleteById(activityId);
        log.info("秒杀活动已删除 - id: {}", activityId);
    }

    @Override
    public void addSeckillProduct(Long merchantId, Long activityId, SeckillProductDTO dto) {
        // 校验活动存在且未结束
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        if (activity.getStatus() == 2) {
            throw new RuntimeException("活动已结束，无法添加商品");
        }

        // 校验商品归属当前商家（selectById 受 @TableLogic 影响，只查 is_on_sale=1 的商品）
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("商品不存在或无权操作");
        }

        // 校验秒杀价必须低于原价
        if (dto.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new RuntimeException("秒杀价必须低于商品原价 ￥" + product.getPrice());
        }

        // 校验同一商品不能重复添加到同一活动
        Long exists = seckillProductMapper.selectCount(
                new LambdaQueryWrapper<SeckillProduct>()
                        .eq(SeckillProduct::getActivityId, activityId)
                        .eq(SeckillProduct::getProductId, dto.getProductId())
        );
        if (exists > 0) {
            throw new RuntimeException("该商品已在此活动中");
        }

        SeckillProduct sp = new SeckillProduct();
        sp.setActivityId(activityId);
        sp.setProductId(dto.getProductId());
        sp.setSeckillPrice(dto.getSeckillPrice());
        int stock = dto.getSeckillStock() != null ? dto.getSeckillStock() : 0;
        sp.setSeckillStock(stock);
        sp.setSeckillStockInit(stock);
        sp.setLimitPerUser(dto.getLimitPerUser() != null ? dto.getLimitPerUser() : 1);
        sp.setCreatedAt(LocalDateTime.now());

        seckillProductMapper.insert(sp);
        log.info("秒杀商品添加成功 - activityId: {}, productId: {}, seckillPrice: {}", activityId, dto.getProductId(), dto.getSeckillPrice());
    }

    @Override
    public List<SeckillProductVO> listSeckillProducts(Long merchantId, Long activityId) {
        return seckillProductMapper.selectByActivityAndMerchant(activityId, merchantId);
    }

    @Override
    public void removeSeckillProduct(Long merchantId, Long activityId, Long spId) {
        SeckillProduct sp = seckillProductMapper.selectById(spId);
        if (sp == null || !sp.getActivityId().equals(activityId)) {
            throw new RuntimeException("秒杀商品不存在");
        }

        // 校验商品归属当前商家
        Product product = productMapper.selectById(sp.getProductId());
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作");
        }

        seckillProductMapper.deleteById(spId);
        log.info("秒杀商品已移除 - spId: {}, activityId: {}", spId, activityId);
    }

    @Override
    public List<SeckillProductVO> listActiveSeckillProducts() {
        refreshStatus();
        // 缓存穿透防护：空值缓存方案
        // 缓存的是列表，这里用一个固定 Key，通过 getWithPassThrough 的空 ID 占位语义不重要
        @SuppressWarnings("unchecked")
        List<SeckillProductVO> result = (List<SeckillProductVO>) (List<?>) cacheClient.getWithPassThrough(
                CACHE_ACTIVE_PRODUCTS, "",
                List.class,
                ignored -> seckillProductMapper.selectActiveSeckillProducts(),
                CACHE_ACTIVE_TTL_SECONDS, TimeUnit.SECONDS
        );
        return result != null ? result : List.of();
    }

    @Override
    public SeckillProductVO getSeckillProductById(Long spId) {
        // 缓存击穿预防：逻辑过期方案
        // 如果未在缓存中（未预热），降级直接查 DB
        SeckillProductVO cached = cacheClient.getWithLogicalExpiry(
                CACHE_PRODUCT_PREFIX, spId,
                SeckillProductVO.class,
                id -> seckillProductMapper.selectActiveById(id),
                CACHE_PRODUCT_TTL_SECONDS, TimeUnit.SECONDS
        );
        if (cached != null) {
            return cached;
        }
        // 未命中（缓存尚未预热）—— 降级查数据库
        return seckillProductMapper.selectActiveById(spId);
    }

    /** 刷新活动状态：根据当前时间自动纠正 status */
    private void refreshStatus() {
        activityMapper.activateStarted();
        activityMapper.deactivateEnded();
    }

    @Override
    public SeckillActivityStatsVO getSeckillStats(Long merchantId, Long activityId) {
        SeckillActivityStatsVO stats = orderMapper.selectSeckillStatsByActivity(merchantId, activityId);
        if (stats == null) {
            // 活动不存在时返回空统计对象
            stats = new SeckillActivityStatsVO();
            stats.setActivityId(activityId);
        }
        return stats;
    }

    private String statusText(Integer status) {
        return switch (status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }
}
