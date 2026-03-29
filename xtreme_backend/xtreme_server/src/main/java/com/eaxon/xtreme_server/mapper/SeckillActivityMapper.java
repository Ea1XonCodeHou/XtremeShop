package com.eaxon.xtreme_server.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eaxon.xtreme_pojo.entity.SeckillActivity;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    /** 将已到开始时间但 status=0 的活动更新为进行中 */
    @Update("UPDATE seckill_activity SET status = 1 WHERE status = 0 AND start_time <= NOW() AND end_time > NOW()")
    int activateStarted();

    /** 将已过结束时间但 status!=2 的活动更新为已结束 */
    @Update("UPDATE seckill_activity SET status = 2 WHERE status != 2 AND end_time <= NOW()")
    int deactivateEnded();
}
