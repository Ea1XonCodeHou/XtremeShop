package com.eaxon.xtreme_server.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.eaxon.xtreme_pojo.vo.MerchantDashboardOverviewVO;
import com.eaxon.xtreme_pojo.vo.MerchantRecentOrderVO;
import com.eaxon.xtreme_server.mapper.OrderMapper;
import com.eaxon.xtreme_server.mapper.ProductMapper;
import com.eaxon.xtreme_server.service.MerchantDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MerchantDashboardServiceImpl implements MerchantDashboardService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public MerchantDashboardOverviewVO getOverview(Long merchantId, int recentLimit) {
        MerchantDashboardOverviewVO vo = new MerchantDashboardOverviewVO();

        BigDecimal totalRevenue = orderMapper.sumPaidAmountByMerchant(merchantId);
        int totalOrders = orderMapper.countPaidOrdersByMerchant(merchantId);
        int totalProducts = productMapper.countOnSaleByMerchant(merchantId);
        List<MerchantRecentOrderVO> recentOrders = orderMapper.selectRecentOrdersByMerchant(merchantId, recentLimit);

        vo.setTotalRevenue(totalRevenue == null ? BigDecimal.ZERO : totalRevenue);
        vo.setTotalOrders(totalOrders);
        vo.setTotalProducts(totalProducts);
        vo.setRecentOrders(recentOrders);
        return vo;
    }
}
