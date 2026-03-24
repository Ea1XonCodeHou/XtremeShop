package com.eaxon.xtreme_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM merchant WHERE phone = #{phone} LIMIT 1")
    Merchant selectByPhone(String phone);
}
