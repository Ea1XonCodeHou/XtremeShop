package com.eaxon.xtreme_server.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_pojo.dto.CategoryDTO;
import com.eaxon.xtreme_pojo.entity.Category;
import com.eaxon.xtreme_server.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 商家端分类管理：分类为全局共享资源，商家可增删改；
 * 公开读取使用 /api/category/list（无需认证）。
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/categories")
@RequiredArgsConstructor
public class MerchantCategoryController {

    private final CategoryService categoryService;

    /** 获取全部分类列表（商家端下拉选择用） */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    /** 新增分类 */
    @PostMapping
    public Result<Void> create(@RequestBody CategoryDTO dto) {
        log.info("新增分类: {}", dto.getName());
        categoryService.create(dto);
        return Result.success();
    }

    /** 修改分类信息（仅传需要修改的字段，其余保持不变） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return Result.success();
    }

    /** 删除分类 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
