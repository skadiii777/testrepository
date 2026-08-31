package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Supplier;

/**
 * 供应商 服务层
 * 
 * @author biz
 */
public interface ISupplierService 
{
    public Supplier selectSupplierById(Long id);

    public List<Supplier> selectSupplierList(Supplier supplier);

    public int insertSupplier(Supplier supplier);

    public int updateSupplier(Supplier supplier);

    public int deleteSupplierByIds(String ids);
}
