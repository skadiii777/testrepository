package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.StockMapper;
import com.ruoyi.business.service.IStockService;

/**
 * 库存 服务层实现
 * 
 * @author biz
 */
@Service
public class StockServiceImpl implements IStockService
{
    @Autowired
    private StockMapper stockMapper;

    @Override
    public Stock selectStockById(Long id)
    {
        return stockMapper.selectStockById(id);
    }

    @Override
    public List<Stock> selectStockList(Stock stock)
    {
        return stockMapper.selectStockList(stock);
    }

    @Override
    public int insertStock(Stock stock)
    {
        return stockMapper.insertStock(stock);
    }

    @Override
    public int updateStock(Stock stock)
    {
        return stockMapper.updateStock(stock);
    }

    @Override
    public int deleteStockByIds(String ids)
    {
        return stockMapper.deleteStockByIds(Convert.toStrArray(ids));
    }


    @Override
    public Long findQuantity(String productName, String warehouse)
    {
        Stock query = new Stock();
        query.setProductName(productName);
        query.setWarehouse(warehouse);
        Stock stock = stockMapper.selectStockByProductAndWarehouse(query);
        return stock == null || stock.getQuantity() == null ? 0L : stock.getQuantity();
    }

    @Override
    public synchronized Long changeStock(String productName, String warehouse, Long delta)
    {
        Stock query = new Stock();
        query.setProductName(productName);
        query.setWarehouse(warehouse);
        Stock stock = stockMapper.selectStockByProductAndWarehouse(query);
        if (stock == null)
        {
            stock = new Stock();
            stock.setProductName(productName);
            stock.setWarehouse(warehouse);
            stock.setQuantity(0L);
            stock.setMinQuantity(0L);
            stockMapper.insertStock(stock);
        }
        long newQty = (stock.getQuantity() == null ? 0L : stock.getQuantity()) + delta;
        if (newQty < 0)
        {
            return -1L;
        }
        Stock update = new Stock();
        update.setId(stock.getId());
        update.setQuantity(newQty);
        stockMapper.updateStock(update);
        return newQty;
    }
}
