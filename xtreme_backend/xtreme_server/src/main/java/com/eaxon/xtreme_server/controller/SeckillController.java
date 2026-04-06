package com.eaxon.xtreme_server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.SeckillActivityDTO;
import com.eaxon.xtreme_pojo.dto.SeckillProductDTO;
import com.eaxon.xtreme_pojo.vo.SeckillActivityStatsVO;
import com.eaxon.xtreme_pojo.vo.SeckillActivityVO;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.SeckillService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/merchant/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    /** 创建秒杀活动 */
    @PostMapping("/activities")
    public Result<Void> createActivity(@RequestBody SeckillActivityDTO dto) {
        log.info("创建秒杀活动: {}", dto.getName());
        seckillService.createActivity(dto);
        return Result.success();
    }

    /** 活动列表（含本商家在各活动中的商品数量） */
    @GetMapping("/activities")
    public Result<List<SeckillActivityVO>> listActivities() {
        Long merchantId = BaseContext.getCurrentId();
        return Result.success(seckillService.listActivities(merchantId));
    }

    /** 删除活动（需先移除所有秒杀商品） */
    @DeleteMapping("/activities/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        seckillService.deleteActivity(id);
        return Result.success();
    }

    /** 向活动中添加秒杀商品 */
    @PostMapping("/activities/{activityId}/products")
    public Result<Void> addSeckillProduct(@PathVariable Long activityId,
                                           @RequestBody SeckillProductDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        seckillService.addSeckillProduct(merchantId, activityId, dto);
        return Result.success();
    }

    /** 查看活动内本商家的秒杀商品 */
    @GetMapping("/activities/{activityId}/products")
    public Result<List<SeckillProductVO>> listSeckillProducts(@PathVariable Long activityId) {
        Long merchantId = BaseContext.getCurrentId();
        return Result.success(seckillService.listSeckillProducts(merchantId, activityId));
    }

    /** 从活动中移除秒杀商品 */
    @DeleteMapping("/activities/{activityId}/products/{spId}")
    public Result<Void> removeSeckillProduct(@PathVariable Long activityId,
                                              @PathVariable Long spId) {
        Long merchantId = BaseContext.getCurrentId();
        seckillService.removeSeckillProduct(merchantId, activityId, spId);
        return Result.success();
    }

    /** 查询本商家在指定秒杀活动中的订单汇总统计（按活动维度） */
    @GetMapping("/activities/{activityId}/stats")
    public Result<SeckillActivityStatsVO> getSeckillStats(@PathVariable Long activityId) {
        Long merchantId = BaseContext.getCurrentId();
        return Result.success(seckillService.getSeckillStats(merchantId, activityId));
    }
}
