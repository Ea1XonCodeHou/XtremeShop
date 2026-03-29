package com.eaxon.xtreme_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_common.annotation.AutoFill;
import com.eaxon.xtreme_common.enums.OperationType;
import com.eaxon.xtreme_pojo.entity.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} AND is_on_sale != -1 ORDER BY created_at DESC")
    List<Product> selectByMerchantId(Long merchantId);

    @AutoFill(OperationType.INSERT)
    @Insert("INSERT INTO product(merchant_id, category_id, name, description, cover_url, price, stock, sold_count, created_at, updated_at, is_on_sale) " +
            "VALUES(#{merchantId}, #{categoryId}, #{name}, #{description}, #{coverUrl}, #{price}, #{stock}, 0, #{createdAt}, #{updatedAt}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertProduct(Product product);

    @AutoFill(OperationType.UPDATE)
    @Select("UPDATE product SET name=#{name}, description=#{description}, cover_url=#{coverUrl}, price=#{price}, stock=#{stock}, category_id=#{categoryId}, updated_at=#{updatedAt} WHERE id=#{id} AND merchant_id=#{merchantId}")
    int updateProduct(Product product);
}
