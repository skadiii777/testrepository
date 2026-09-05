package com.enterprise.module.biz.service.supplier;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.supplier.vo.supplier.SupplierPageReqVO;
import com.enterprise.module.biz.controller.admin.supplier.vo.supplier.SupplierSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.supplier.SupplierDO;
import com.enterprise.module.biz.dal.mysql.supplier.SupplierMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 供应商 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class SupplierServiceImpl implements SupplierService {

    @Resource
    private SupplierMapper supplierMapper;

    @Override
    public Long createSupplier(SupplierSaveReqVO createReqVO) {
        SupplierDO supplier = BeanUtils.toBean(createReqVO, SupplierDO.class);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }


    @Override
    public void updateSupplier(SupplierSaveReqVO updateReqVO) {
        validateSupplierExists(updateReqVO.getId());
        SupplierDO updateObj = BeanUtils.toBean(updateReqVO, SupplierDO.class);
        supplierMapper.updateById(updateObj);
    }


    @Override
    public void deleteSupplier(Long id) {
        validateSupplierExists(id);
        supplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if (supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public SupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public PageResult<SupplierDO> getSupplierPage(SupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }
}