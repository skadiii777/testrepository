package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.StockMove;

/**
 * 库存流水 数据层
 *
 * @author biz
 */
public interface StockMoveMapper
{
    public List<StockMove> selectStockMoveList(StockMove stockMove);

    /** 插入一条不可变流水（不提供修改/删除，流水只增不改） */
    public int insertStockMove(StockMove stockMove);
}
