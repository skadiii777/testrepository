package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Sales;
import com.ruoyi.business.mapper.SalesMapper;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.business.service.ISalesService;

/**
 * 销售单 服务层实现
 * 
 * @author biz
 */
@Service
public class SalesServiceImpl implements ISalesService
{
    @Autowired
    private SalesMapper salesMapper;

    @Autowired
    private IStockService stockService;

    @Override
    public Sales selectSalesById(Long id)
    {
        return salesMapper.selectSalesById(id);
    }

    @Override
    public List<Sales> selectSalesList(Sales sales)
    {
        return salesMapper.selectSalesList(sales);
    }

    @Override
    public int insertSales(Sales sales)
    {
        int rows = salesMapper.insertSales(sales);
        if ("1".equals(sales.getStatus()) && rows > 0)
        {
            stockService.changeStock(sales.getProductName(), "默认仓库", -sales.getQuantity());
        }
        return rows;
    }

    /**
     * 校验库存是否满足出库（status=1 时）
     */
    @Override
    public boolean checkStockEnough(Sales sales)
    {
        if (!"1".equals(sales.getStatus()))
        {
            return true;
        }
        if (sales.getQuantity() == null || sales.getProductName() == null)
        {
            return false;
        }
        return stockService.findQuantity(sales.getProductName(), "默认仓库") >= sales.getQuantity();
    }

    @Override
    public int updateSales(Sales sales)
    {
        return salesMapper.updateSales(sales);
    }

    @Override
    public int deleteSalesByIds(String ids)
    {
        return salesMapper.deleteSalesByIds(Convert.toStrArray(ids));
    }

}
