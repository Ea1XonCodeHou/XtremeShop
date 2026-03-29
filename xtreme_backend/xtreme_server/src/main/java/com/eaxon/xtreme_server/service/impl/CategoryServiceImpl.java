package com.eaxon.xtreme_server.service.impl;

import com.eaxon.xtreme_pojo.entity.Category;
import com.eaxon.xtreme_server.mapper.CategoryMapper;
import com.eaxon.xtreme_server.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectAllOrdered();
    }
}
