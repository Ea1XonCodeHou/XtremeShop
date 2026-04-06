package com.eaxon.xtreme_server.service;

import java.util.List;

import com.eaxon.xtreme_pojo.dto.CategoryDTO;
import com.eaxon.xtreme_pojo.entity.Category;

public interface CategoryService {
    List<Category> listAll();
    void create(CategoryDTO dto);
    void update(Long id, CategoryDTO dto);
    void delete(Long id);
}
