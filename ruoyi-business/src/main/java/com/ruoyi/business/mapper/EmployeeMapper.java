package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.Employee;

/**
 * 员工 数据层
 * 
 * @author biz
 */
public interface EmployeeMapper
{
    public Employee selectEmployeeById(Long id);

    public List<Employee> selectEmployeeList(Employee employee);

    public int insertEmployee(Employee employee);

    public int updateEmployee(Employee employee);

    public int deleteEmployeeByIds(String[] ids);
}
