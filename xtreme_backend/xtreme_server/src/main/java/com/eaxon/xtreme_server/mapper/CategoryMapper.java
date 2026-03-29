package com.eaxon.xtreme_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM category ORDER BY sort_order ASC")
    List<Category> selectAllOrdered();
}
