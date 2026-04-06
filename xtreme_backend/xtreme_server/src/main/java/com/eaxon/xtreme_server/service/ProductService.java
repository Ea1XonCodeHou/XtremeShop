package com.eaxon.xtreme_server.service;

import java.util.List;

import com.eaxon.xtreme_pojo.dto.ProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_pojo.vo.ProductVO;

public interface ProductService {
    List<Product> listByMerchant(Long merchantId);
    void create(Long merchantId, ProductDTO dto);
    void update(Long merchantId, Long productId, ProductDTO dto);
    void toggleSale(Long merchantId, Long productId);
    void delete(Long merchantId, Long productId);
    /** 公开首页：分页查询上架商品，可按分类过滤 */
    PageResult<ProductVO> listPublicProducts(Long categoryId, int page, int pageSize);
}
