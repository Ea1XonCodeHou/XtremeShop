package com.eaxon.xtreme_server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;
import com.eaxon.xtreme_server.service.SeckillService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillPublicController {

    private final SeckillService seckillService;

    /** 首页：获取当前进行中活动的全部秒杀商品 */
    @GetMapping("/active")
    public Result<List<SeckillProductVO>> activeSeckillProducts() {
        return Result.success(seckillService.listActiveSeckillProducts());
    }

    /** 单个秒杀商品详情（下单确认页用） */
    @GetMapping("/product/{spId}")
    public Result<SeckillProductVO> getSeckillProduct(@PathVariable Long spId) {
        SeckillProductVO vo = seckillService.getSeckillProductById(spId);
        if (vo == null) {
            throw new RuntimeException("秒杀商品不存在");
        }
        return Result.success(vo);
    }
}
