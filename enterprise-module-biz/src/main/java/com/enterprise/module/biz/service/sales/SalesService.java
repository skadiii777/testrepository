package com.enterprise.module.biz.service.sales;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesPageReqVO;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;

/**
 * 销售单 Service 接口
 *
 * @author 企业管理平台
 */
public interface SalesService {

    /**
     * 创建销售单（强制草稿状态）
     */
    Long createSales(SalesSaveReqVO createReqVO);

    /**
     * 状态流转：0草稿→1已确认 / 0,1→3已作废
     */
    void transitionSales(Long id, String action);

    /**
     * 完成销售单（1已确认→2已完成，自动出库，库存不足拒绝）
     */
    void completeSales(Long id);

    /**
     * 更新销售单
     */
    void updateSales(SalesSaveReqVO updateReqVO);

    /**
     * 删除销售单
     */
    void deleteSales(Long id);

    /**
     * 获得销售单
     */
    SalesDO getSales(Long id);

    /**
     * 获得销售单分页
     */
    PageResult<SalesDO> getSalesPage(SalesPageReqVO pageReqVO);

    /**
     * 校验库存是否满足出库
     */
    void validateStockEnough(String productName, Long quantity);
}