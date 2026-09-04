package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.StockMove;
import com.ruoyi.business.mapper.StockMoveMapper;
import com.ruoyi.business.service.IStockMoveService;

/**
 * 库存流水 服务层实现
 *
 * @author biz
 */
@Service
public class StockMoveServiceImpl implements IStockMoveService
{
    @Autowired
    private StockMoveMapper stockMoveMapper;

    @Override
    public List<StockMove> selectStockMoveList(StockMove stockMove)
    {
        return stockMoveMapper.selectStockMoveList(stockMove);
    }

    @Override
    public void record(String productName, String warehouse, String moveType,
                       BigDecimal quantity, BigDecimal balanceAfter,
                       String sourceType, String sourceCode, String createBy)
    {
        StockMove move = new StockMove();
        move.setProductName(productName);
        move.setWarehouse(warehouse == null ? "默认仓库" : warehouse);
        move.setMoveType(moveType);
        move.setQuantity(quantity == null ? BigDecimal.ZERO : quantity.abs());
        move.setBalanceAfter(balanceAfter == null ? BigDecimal.ZERO : balanceAfter);
        move.setSourceType(sourceType == null ? "manual" : sourceType);
        move.setSourceCode(sourceCode == null ? "" : sourceCode);
        move.setCreateBy(createBy == null ? "system" : createBy);
        stockMoveMapper.insertStockMove(move);
    }
}
