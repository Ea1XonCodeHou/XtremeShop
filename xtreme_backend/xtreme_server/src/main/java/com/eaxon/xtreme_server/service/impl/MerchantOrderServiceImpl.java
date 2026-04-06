package com.eaxon.xtreme_server.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eaxon.xtreme_pojo.vo.MerchantOrderVO;
import com.eaxon.xtreme_pojo.vo.PageResult;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.service.MerchantOrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private final OrderMapper orderMapper;

    @Override
    public PageResult<MerchantOrderVO> listOrders(Long merchantId, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<MerchantOrderVO> list;
        long total;

        if (status != null) {
            list  = orderMapper.selectOrdersByMerchantAndStatus(merchantId, status, pageSize, offset);
            total = orderMapper.countOrdersByMerchantAndStatus(merchantId, status);
        } else {
            list  = orderMapper.selectOrdersByMerchant(merchantId, pageSize, offset);
            total = orderMapper.countOrdersByMerchant(merchantId);
        }

        return PageResult.of(list, total);
    }
}
