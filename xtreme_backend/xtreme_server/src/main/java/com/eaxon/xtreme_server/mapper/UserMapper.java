package com.eaxon.xtreme_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_common.annotation.AutoFill;
import com.eaxon.xtreme_common.enums.OperationType;
import com.eaxon.xtreme_pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE phone = #{phone} LIMIT 1")
    User selectByPhone(String phone);

    @AutoFill(OperationType.INSERT)
    int insertUser(User user);

    @AutoFill(OperationType.UPDATE)
    int updateUser(User user);
}
