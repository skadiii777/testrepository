package com.ruoyi.business.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.business.domain.Employee;
import com.ruoyi.business.service.IEmployeeService;

/**
 * 员工 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/employee")
public class EmployeeController extends BaseController
{
    private String prefix = "biz/employee";

    @Autowired
    private IEmployeeService employeeService;

    @RequiresPermissions("biz:employee:view")
    @GetMapping()
    public String employee()
    {
        return prefix + "/employee";
    }

    @RequiresPermissions("biz:employee:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Employee employee)
    {
        startPage();
        List<Employee> list = employeeService.selectEmployeeList(employee);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:employee:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:employee:add")
    @Log(title = "员工", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Employee employee)
    {
        employee.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(employeeService.insertEmployee(employee));
    }

    @RequiresPermissions("biz:employee:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("employee", employeeService.selectEmployeeById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:employee:edit")
    @Log(title = "员工", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Employee employee)
    {
        employee.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(employeeService.updateEmployee(employee));
    }

    @RequiresPermissions("biz:employee:remove")
    @Log(title = "员工", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(employeeService.deleteEmployeeByIds(ids));
    }
}
