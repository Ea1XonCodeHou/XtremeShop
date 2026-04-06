package com.eaxon.xtreme_server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eaxon.xtreme_pojo.dto.ProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_pojo.vo.ProductVO;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public List<Product> listByMerchant(Long merchantId) {
        return productMapper.selectByMerchantId(merchantId);
    }

    @Override
    public void create(Long merchantId, ProductDTO dto) {
        Product product = new Product();
        product.setMerchantId(merchantId);
        product.setCategoryId(dto.getCategoryId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCoverUrl(dto.getCoverUrl());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        // createdAt/updatedAt 由 @AutoFill 切面自动填充
        productMapper.insertProduct(product);
    }

    @Override
    public void update(Long merchantId, Long productId, ProductDTO dto) {
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
        );
        if (product == null) throw new RuntimeException("商品不存在或无权操作");

        if (dto.getName()        != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getCoverUrl()    != null) product.setCoverUrl(dto.getCoverUrl());
        if (dto.getPrice()       != null) product.setPrice(dto.getPrice());
        if (dto.getStock()       != null) product.setStock(dto.getStock());
        if (dto.getCategoryId()  != null) product.setCategoryId(dto.getCategoryId());
        // updatedAt 由 @AutoFill 切面自动填充
        productMapper.updateProduct(product);
    }

    @Override
    public void toggleSale(Long merchantId, Long productId) {
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
        );
        if (product == null) throw new RuntimeException("商品不存在");

        int newStatus = product.getIsOnSale() == 1 ? 0 : 1;
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
                        .set(Product::getIsOnSale, newStatus)
        );
    }

    @Override
    public void delete(Long merchantId, Long productId) {
        productMapper.delete(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getMerchantId, merchantId)
        );
    }

    @Override
    public PageResult<ProductVO> listPublicProducts(Long categoryId, int page, int pageSize) {
        int offset = Math.max(0, (page - 1) * pageSize);
        List<ProductVO> list = productMapper.selectPublicOnSale(categoryId, pageSize, offset);
        int total = productMapper.countPublicOnSale(categoryId);
        return PageResult.of(list, total);
    }
}
