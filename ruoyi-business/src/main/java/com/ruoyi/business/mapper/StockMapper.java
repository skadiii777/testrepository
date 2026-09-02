package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Stock;

/**
 * 库存 数据层
 * 
 * @author biz
 */
public interface StockMapper
{
    public Stock selectStockById(Long id);

    public List<Stock> selectStockList(Stock stock);

    public int insertStock(Stock stock);

    public int updateStock(Stock stock);

    public int deleteStockByIds(String[] ids);

    /**
     * 根据产品名称和仓库查询库存
     */
    public Stock selectStockByProductAndWarehouse(Stock stock);

    /**
     * 查询库存低于预警下限的记录
     */
    public List<Stock> selectLowStockList();
}
