package com.eaxon.xtreme_pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String username;

    private String password;

    private String salt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 逻辑删除字段：1=活跃，0=禁用
    @TableLogic
    private Integer isActive;
}
