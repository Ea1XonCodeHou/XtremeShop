package com.eaxon.xtreme_server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eaxon.xtreme_pojo.dto.SeckillActivityDTO;
import com.eaxon.xtreme_pojo.dto.SeckillProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.entity.SeckillActivity;
import com.eaxon.xtreme_pojo.entity.SeckillProduct;
import com.eaxon.xtreme_pojo.vo.SeckillActivityVO;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.mapper.SeckillActivityMapper;
import com.eaxon.xtreme_server.mapper.SeckillProductMapper;
import com.eaxon.xtreme_server.service.SeckillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper productMapper;

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
        return seckillProductMapper.selectActiveSeckillProducts();
    }

    @Override
    public SeckillProductVO getSeckillProductById(Long spId) {
        return seckillProductMapper.selectActiveById(spId);
    }

    /** 刷新活动状态：根据当前时间自动纠正 status */
    private void refreshStatus() {
        activityMapper.activateStarted();
        activityMapper.deactivateEnded();
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
