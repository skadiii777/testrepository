package com.enterprise.module.biz.service.employee;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeePageReqVO;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeeSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;

/**
 * 员工 Service 接口
 *
 * @author 企业管理平台
 */
public interface EmployeeService {

    /**
     * 创建员工
     */
    Long createEmployee(EmployeeSaveReqVO createReqVO);

    /**
     * 更新员工
     */
    void updateEmployee(EmployeeSaveReqVO updateReqVO);

    /**
     * 删除员工
     */
    void deleteEmployee(Long id);

    /**
     * 获得员工
     */
    EmployeeDO getEmployee(Long id);

    /**
     * 获得员工分页
     */
    PageResult<EmployeeDO> getEmployeePage(EmployeePageReqVO pageReqVO);

}