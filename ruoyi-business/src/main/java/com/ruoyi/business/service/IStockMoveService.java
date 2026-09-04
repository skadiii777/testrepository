package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.StockMove;

/**
 * 库存流水 服务层
 *
 * @author biz
 */
public interface IStockMoveService
{
    public List<StockMove> selectStockMoveList(StockMove stockMove);

    /**
     * 记录一条库存流水（内部调用）
     *
     * @param productName 产品
     * @param warehouse 仓库
     * @param moveType 1入库 2出库
     * @param quantity 数量（正数）
     * @param balanceAfter 操作后结余
     * @param sourceType 来源类型（purchase/sales/manual）
     * @param sourceCode 来源单号
     * @param createBy 操作人
     */
    public void record(String productName, String warehouse, String moveType,
                       java.math.BigDecimal quantity, java.math.BigDecimal balanceAfter,
                       String sourceType, String sourceCode, String createBy);
}
