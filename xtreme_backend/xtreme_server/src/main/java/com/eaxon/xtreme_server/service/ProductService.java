package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.dto.ProductDTO;
import com.eaxon.xtreme_pojo.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> listByMerchant(Long merchantId);
    void create(Long merchantId, ProductDTO dto);
    void toggleSale(Long merchantId, Long productId);
    void delete(Long merchantId, Long productId);
}
