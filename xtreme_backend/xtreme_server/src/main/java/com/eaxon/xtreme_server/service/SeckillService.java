package com.eaxon.xtreme_server.service;

import java.util.List;

import com.eaxon.xtreme_pojo.dto.SeckillActivityDTO;
import com.eaxon.xtreme_pojo.dto.SeckillProductDTO;
import com.eaxon.xtreme_pojo.vo.SeckillActivityVO;
import com.eaxon.xtreme_pojo.vo.SeckillProductVO;

public interface SeckillService {

    void createActivity(SeckillActivityDTO dto);

    List<SeckillActivityVO> listActivities(Long merchantId);

    void deleteActivity(Long activityId);

    void addSeckillProduct(Long merchantId, Long activityId, SeckillProductDTO dto);

    List<SeckillProductVO> listSeckillProducts(Long merchantId, Long activityId);

    void removeSeckillProduct(Long merchantId, Long activityId, Long spId);

    List<SeckillProductVO> listActiveSeckillProducts();

    SeckillProductVO getSeckillProductById(Long spId);
}
