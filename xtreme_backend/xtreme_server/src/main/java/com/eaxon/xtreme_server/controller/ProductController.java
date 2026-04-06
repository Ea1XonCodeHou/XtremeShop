package com.eaxon.xtreme_server.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eaxon.xtreme_common.result.Result;
import com.eaxon.xtreme_common.utils.AliOssUtil;
import com.eaxon.xtreme_pojo.dto.ProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_server.context.BaseContext;
import com.eaxon.xtreme_server.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AliOssUtil aliOssUtil;

    /** 上传图片到 OSS，返回公网 URL（商品封面、店铺 Logo 统一走这个接口） */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = "images/" + UUID.randomUUID().toString().replace("-", "") + ext;
        String url = aliOssUtil.uploadFile(fileName, file.getInputStream());
        log.info("OSS 上传成功，商家ID: {}, url: {}", BaseContext.getCurrentId(), url);
        return Result.success(url);
    }

    /** 获取当前商家的商品列表 */
    @GetMapping("/products")
    public Result<List<Product>> list() {
        Long merchantId = BaseContext.getCurrentId();
        return Result.success(productService.listByMerchant(merchantId));
    }

    /** 发布新商品 */
    @PostMapping("/products")
    public Result<Void> create(@RequestBody ProductDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        productService.create(merchantId, dto);
        return Result.success();
    }

    /** 编辑商品信息（名称/简介/封面/价格/库存/分类，不传字段则保持不变） */
    @PutMapping("/products/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        Long merchantId = BaseContext.getCurrentId();
        productService.update(merchantId, id, dto);
        return Result.success();
    }

    /** 上架/下架 */
    @PutMapping("/products/{id}/toggle")
    public Result<Void> toggleSale(@PathVariable Long id) {
        Long merchantId = BaseContext.getCurrentId();
        productService.toggleSale(merchantId, id);
        return Result.success();
    }

    /** 删除商品 */
    @DeleteMapping("/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long merchantId = BaseContext.getCurrentId();
        productService.delete(merchantId, id);
        return Result.success();
    }
}
