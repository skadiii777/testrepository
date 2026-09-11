package com.enterprise.module.biz.service.support;

import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.dataobject.product.ProductDO;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;
import com.enterprise.module.biz.dal.dataobject.warehouse.WarehouseDO;
import com.enterprise.module.biz.dal.mysql.product.ProductMapper;
import com.enterprise.module.biz.dal.mysql.employee.EmployeeMapper;
import com.enterprise.module.biz.dal.mysql.warehouse.WarehouseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/** Legacy name-only requests are accepted only when the name resolves uniquely inside the tenant. */
@Service
public class BizReferenceService {
    @Resource private ProductMapper productMapper;
    @Resource private EmployeeMapper employeeMapper;
    @Resource private WarehouseMapper warehouseMapper;
    public ProductDO product(Long id, String name) {
        ProductDO value;
        if (id != null) value = productMapper.selectById(id);
        else {
            if (name == null || name.isBlank()) throw exception(MASTER_REFERENCE_INVALID);
            List<ProductDO> rows = productMapper.selectList(new LambdaQueryWrapperX<ProductDO>()
                    .eq(ProductDO::getProductName, name).last("LIMIT 2"));
            value = rows.size() == 1 ? rows.get(0) : null;
        }
        if (value == null) throw exception(MASTER_REFERENCE_INVALID);
        return value;
    }
    public EmployeeDO employee(Long id, String name) {
        EmployeeDO value;
        if (id != null) value = employeeMapper.selectById(id);
        else {
            if (name == null || name.isBlank()) throw exception(MASTER_REFERENCE_INVALID);
            List<EmployeeDO> rows = employeeMapper.selectList(new LambdaQueryWrapperX<EmployeeDO>()
                    .eq(EmployeeDO::getEmpName, name).last("LIMIT 2"));
            value = rows.size() == 1 ? rows.get(0) : null;
        }
        if (value == null) throw exception(MASTER_REFERENCE_INVALID);
        return value;
    }
    public EmployeeDO findEmployeeForUser(Long userId) {
        if (userId == null) return null;
        return employeeMapper.selectOne(new LambdaQueryWrapperX<EmployeeDO>().eq(EmployeeDO::getUserId,userId));
    }
    public EmployeeDO employeeForUser(Long userId) {
        if (userId == null) throw exception(MASTER_REFERENCE_INVALID);
        var value = findEmployeeForUser(userId);
        if (value == null) throw exception(MASTER_REFERENCE_INVALID);
        return value;
    }
    public WarehouseDO warehouse(Long id, String name) {
        WarehouseDO value = id != null ? warehouseMapper.selectById(id)
                : warehouseMapper.selectOne(new LambdaQueryWrapperX<WarehouseDO>()
                        .eq(WarehouseDO::getName, name == null || name.isBlank() ? "默认仓库" : name));
        if (value == null) throw exception(MASTER_REFERENCE_INVALID);
        return value;
    }
}
