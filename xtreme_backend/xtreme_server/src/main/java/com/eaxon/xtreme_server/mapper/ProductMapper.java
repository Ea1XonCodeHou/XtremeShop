package com.eaxon.xtreme_server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_common.annotation.AutoFill;
import com.eaxon.xtreme_common.enums.OperationType;
import com.eaxon.xtreme_pojo.entity.Product;
import com.eaxon.xtreme_pojo.vo.ProductVO;

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
    @Update("UPDATE product SET name=#{name}, description=#{description}, cover_url=#{coverUrl}, price=#{price}, stock=#{stock}, category_id=#{categoryId}, updated_at=#{updatedAt} WHERE id=#{id} AND merchant_id=#{merchantId}")
    int updateProduct(Product product);

    /** 统计商家在售商品数 */
    @Select("SELECT COUNT(*) FROM product WHERE merchant_id = #{merchantId} AND is_on_sale = 1")
    int countOnSaleByMerchant(@Param("merchantId") Long merchantId);

    /**
     * 支付成功后更新商品销售指标：销量+quantity，普通库存在足够时-quantity。
     *
     * <p>WHERE 条件包含 stock >= quantity，保证不会扣出负库存。
     */
    @Update("UPDATE product SET sold_count = sold_count + #{quantity}, stock = stock - #{quantity} " +
            "WHERE id = #{productId} AND stock >= #{quantity}")
    int increaseSoldCountAndDecreaseStock(@Param("productId") Long productId,
                                          @Param("quantity") Integer quantity);

    /** 当普通库存不足时，至少保证销量统计可见 */
    @Update("UPDATE product SET sold_count = sold_count + #{quantity} WHERE id = #{productId}")
    int increaseSoldCountOnly(@Param("productId") Long productId,
                              @Param("quantity") Integer quantity);

    // ------------------------------------------------------------------ //
    //  公开商品列表（用户首页）
    // ------------------------------------------------------------------ //

    /**
     * 分页查询上架中的商品，支持按分类过滤。
     * categoryId 为 null 时返回全部分类的商品。
     */
    @Select({"<script>",
        "SELECT id, name, description, price, cover_url, category_id, stock, sold_count",
        "FROM product",
        "WHERE is_on_sale = 1",
        "<if test='categoryId != null'>AND category_id = #{categoryId}</if>",
        "ORDER BY created_at DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"})
    List<ProductVO> selectPublicOnSale(@Param("categoryId") Long categoryId,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);

    /** 分类下的上架商品总数 */
    @Select({"<script>",
        "SELECT COUNT(*) FROM product WHERE is_on_sale = 1",
        "<if test='categoryId != null'>AND category_id = #{categoryId}</if>",
        "</script>"})
    int countPublicOnSale(@Param("categoryId") Long categoryId);
}
