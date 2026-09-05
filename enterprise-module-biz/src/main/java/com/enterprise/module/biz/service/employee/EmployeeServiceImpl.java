package com.enterprise.module.biz.service.employee;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeePageReqVO;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeeSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;
import com.enterprise.module.biz.dal.mysql.employee.EmployeeMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 员工 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class EmployeeServiceImpl implements EmployeeService {

    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public Long createEmployee(EmployeeSaveReqVO createReqVO) {
        EmployeeDO employee = BeanUtils.toBean(createReqVO, EmployeeDO.class);
        employeeMapper.insert(employee);
        return employee.getId();
    }


    @Override
    public void updateEmployee(EmployeeSaveReqVO updateReqVO) {
        validateEmployeeExists(updateReqVO.getId());
        EmployeeDO updateObj = BeanUtils.toBean(updateReqVO, EmployeeDO.class);
        employeeMapper.updateById(updateObj);
    }


    @Override
    public void deleteEmployee(Long id) {
        validateEmployeeExists(id);
        employeeMapper.deleteById(id);
    }

    private void validateEmployeeExists(Long id) {
        if (employeeMapper.selectById(id) == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
    }

    @Override
    public EmployeeDO getEmployee(Long id) {
        return employeeMapper.selectById(id);
    }

    @Override
    public PageResult<EmployeeDO> getEmployeePage(EmployeePageReqVO pageReqVO) {
        return employeeMapper.selectPage(pageReqVO);
    }
}