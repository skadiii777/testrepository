package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Stock;

/**
 * 库存 服务层
 * 
 * @author biz
 */
public interface IStockService 
{
    public Stock selectStockById(Long id);

    public List<Stock> selectStockList(Stock stock);

    public int insertStock(Stock stock);

    public int updateStock(Stock stock);

    public int deleteStockByIds(String ids);

    /**
     * 库存变更（delta 正数入库、负数出库），库存记录不存在时自动创建
     *
     * @return 变更后的库存数量，库存不足时返回 -1
     */
    public Long changeStock(String productName, String warehouse, Long delta);

    /**
     * 查询指定产品在默认仓库的当前库存数量（无记录返回0）
     */
    public Long findQuantity(String productName, String warehouse);

    /**
     * 查询库存低于预警下限的记录
     */
    public List<Stock> selectLowStockList();
}
