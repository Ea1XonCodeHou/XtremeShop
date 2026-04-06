package com.eaxon.xtreme_server.service;

import com.eaxon.xtreme_pojo.vo.MerchantOrderVO;
import com.eaxon.xtreme_pojo.vo.PageResult;

/**
 * 商家端订单分页查询服务接口
 */
public interface MerchantOrderService {

    /**
     * 分页查询商家订单列表，可按状态过滤
     *
     * @param merchantId 商家 ID
     * @param status     订单状态过滤（null = 查全部，0=待支付 1=已支付 2=已取消 3=已退款）
     * @param page       页码（从 1 开始）
     * @param pageSize   每页条数
     */
    PageResult<MerchantOrderVO> listOrders(Long merchantId, Integer status, int page, int pageSize);
}
