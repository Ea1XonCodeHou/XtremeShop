package com.eaxon.xtreme_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_pojo.vo.ProductVO;
import com.eaxon.xtreme_server.service.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * 公开商品浏览接口（无需登录）
 * 路径 /api/products 未被任何拦截器覆盖，天然公开。
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    /**
     * 分页获取上架商品列表。
     *
     * @param categoryId 分类过滤（null = 全部分类）
     * @param page       页码，从 1 开始
     * @param pageSize   每页条数（最大 40）
     */
    @GetMapping
    public Result<PageResult<ProductVO>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "12") int pageSize) {

        pageSize = Math.min(pageSize, 40);
        return Result.success(productService.listPublicProducts(categoryId, page, pageSize));
    }
}
