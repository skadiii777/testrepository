package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Employee;
import com.ruoyi.business.mapper.EmployeeMapper;
import com.ruoyi.business.service.IEmployeeService;

/**
 * 员工 服务层实现
 * 
 * @author biz
 */
@Service
public class EmployeeServiceImpl implements IEmployeeService
{
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee selectEmployeeById(Long id)
    {
        return employeeMapper.selectEmployeeById(id);
    }

    @Override
    public List<Employee> selectEmployeeList(Employee employee)
    {
        return employeeMapper.selectEmployeeList(employee);
    }

    @Override
    public int insertEmployee(Employee employee)
    {
        return employeeMapper.insertEmployee(employee);
    }

    @Override
    public int updateEmployee(Employee employee)
    {
        return employeeMapper.updateEmployee(employee);
    }

    @Override
    public int deleteEmployeeByIds(String ids)
    {
        return employeeMapper.deleteEmployeeByIds(Convert.toStrArray(ids));
    }

}
