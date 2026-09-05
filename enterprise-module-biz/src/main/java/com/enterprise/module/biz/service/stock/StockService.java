package com.enterprise.module.biz.service.stock;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.StockPageReqVO;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.StockSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;

/**
 * 库存 Service 接口
 *
 * @author 企业管理平台
 */
public interface StockService {

    /**
     * 创建库存
     */
    Long createStock(StockSaveReqVO createReqVO);

    /**
     * 更新库存
     */
    void updateStock(StockSaveReqVO updateReqVO);

    /**
     * 删除库存
     */
    void deleteStock(Long id);

    /**
     * 获得库存
     */
    StockDO getStock(Long id);

    /**
     * 获得库存分页
     */
    PageResult<StockDO> getStockPage(StockPageReqVO pageReqVO);

    /**
     * 库存变更（delta 正数入库/负数出库），自动落流水；数量不足返回 false
     */
    boolean changeStock(String productName, String warehouse, Long delta, String sourceType, String sourceCode);

    /**
     * 查询库存数量（无记录返回 0）
     */
    Long findQuantity(String productName, String warehouse);

    /**
     * 查询库存低于预警下限的记录
     */
    java.util.List<StockDO> getLowStockList();
}