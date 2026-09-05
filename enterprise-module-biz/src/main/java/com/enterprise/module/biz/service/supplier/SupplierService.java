package com.enterprise.module.biz.service.supplier;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.supplier.vo.supplier.SupplierPageReqVO;
import com.enterprise.module.biz.controller.admin.supplier.vo.supplier.SupplierSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.supplier.SupplierDO;

/**
 * 供应商 Service 接口
 *
 * @author 企业管理平台
 */
public interface SupplierService {

    /**
     * 创建供应商
     */
    Long createSupplier(SupplierSaveReqVO createReqVO);

    /**
     * 更新供应商
     */
    void updateSupplier(SupplierSaveReqVO updateReqVO);

    /**
     * 删除供应商
     */
    void deleteSupplier(Long id);

    /**
     * 获得供应商
     */
    SupplierDO getSupplier(Long id);

    /**
     * 获得供应商分页
     */
    PageResult<SupplierDO> getSupplierPage(SupplierPageReqVO pageReqVO);

}