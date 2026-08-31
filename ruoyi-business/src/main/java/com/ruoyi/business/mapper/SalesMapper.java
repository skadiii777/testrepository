package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Sales;

/**
 * 销售单 数据层
 * 
 * @author biz
 */
public interface SalesMapper
{
    public Sales selectSalesById(Long id);

    public List<Sales> selectSalesList(Sales sales);

    public int insertSales(Sales sales);

    public int updateSales(Sales sales);

    public int deleteSalesByIds(String[] ids);
}
