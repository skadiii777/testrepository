package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Purchase;
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.mapper.PurchaseMapper;
import com.ruoyi.business.service.IStockService;
import com.ruoyi.business.service.IPurchaseService;

/**
 * 采购单 服务层实现
 * 
 * @author biz
 */
@Service
public class PurchaseServiceImpl implements IPurchaseService
{
    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private IStockService stockService;

    @Override
    public Purchase selectPurchaseById(Long id)
    {
        return purchaseMapper.selectPurchaseById(id);
    }

    @Override
    public List<Purchase> selectPurchaseList(Purchase purchase)
    {
        return purchaseMapper.selectPurchaseList(purchase);
    }

    @Override
    public int insertPurchase(Purchase purchase)
    {
        int rows = purchaseMapper.insertPurchase(purchase);
        if ("1".equals(purchase.getStatus()))
        {
            stockService.changeStock(purchase.getProductName(), "默认仓库", purchase.getQuantity());
        }
        return rows;
    }

    @Override
    public int updatePurchase(Purchase purchase)
    {
        return purchaseMapper.updatePurchase(purchase);
    }

    @Override
    public int deletePurchaseByIds(String ids)
    {
        return purchaseMapper.deletePurchaseByIds(Convert.toStrArray(ids));
    }

}
