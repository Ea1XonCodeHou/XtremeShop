package com.eaxon.xtreme_pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant implements Serializable {

    @TableId(type = IdType.INPUT) // 由 RedisIdWorker 生成，非自增
    private Long id;

    private String name;

    private String phone;

    private String password;

    private String salt;

    private String logoUrl;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isActive;
}
