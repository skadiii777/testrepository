package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Sales;

/**
 * 销售单 服务层
 * 
 * @author biz
 */
public interface ISalesService 
{
    public Sales selectSalesById(Long id);

    public List<Sales> selectSalesList(Sales sales);

    public int insertSales(Sales sales);

    public int updateSales(Sales sales);

    public int deleteSalesByIds(String ids);

    public boolean checkStockEnough(Sales sales);
}
