package com.eaxon.xtreme_pojo.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("seckill_activity")
public class SeckillActivity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0=未开始 1=进行中 2=已结束 */
    private Integer status;

    private LocalDateTime createdAt;
}
