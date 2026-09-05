package com.enterprise.module.biz.service.stockmove;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove.StockMovePageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;

/**
 * 库存流水 Service 接口（只增不改）
 *
 * @author 企业管理平台
 */
public interface StockMoveService {

    /**
     * 记录一条库存流水
     */
    void record(StockMoveDO move);

    /**
     * 获得库存流水
     */
    StockMoveDO getStockMove(Long id);

    /**
     * 获得库存流水分页
     */
    PageResult<StockMoveDO> getStockMovePage(StockMovePageReqVO pageReqVO);
}