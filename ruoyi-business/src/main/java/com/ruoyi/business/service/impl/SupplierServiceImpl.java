package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Supplier;
import com.ruoyi.business.mapper.SupplierMapper;
import com.ruoyi.business.service.ISupplierService;

/**
 * 供应商 服务层实现
 * 
 * @author biz
 */
@Service
public class SupplierServiceImpl implements ISupplierService
{
    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public Supplier selectSupplierById(Long id)
    {
        return supplierMapper.selectSupplierById(id);
    }

    @Override
    public List<Supplier> selectSupplierList(Supplier supplier)
    {
        return supplierMapper.selectSupplierList(supplier);
    }

    @Override
    public int insertSupplier(Supplier supplier)
    {
        return supplierMapper.insertSupplier(supplier);
    }

    @Override
    public int updateSupplier(Supplier supplier)
    {
        return supplierMapper.updateSupplier(supplier);
    }

    @Override
    public int deleteSupplierByIds(String ids)
    {
        return supplierMapper.deleteSupplierByIds(Convert.toStrArray(ids));
    }

}
