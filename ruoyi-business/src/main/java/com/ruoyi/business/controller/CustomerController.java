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
import com.ruoyi.business.domain.Customer;
import com.ruoyi.business.service.ICustomerService;

/**
 * 客户 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/customer")
public class CustomerController extends BaseController
{
    private String prefix = "biz/customer";

    @Autowired
    private ICustomerService customerService;

    @RequiresPermissions("biz:customer:view")
    @GetMapping()
    public String customer()
    {
        return prefix + "/customer";
    }

    @RequiresPermissions("biz:customer:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Customer customer)
    {
        startPage();
        List<Customer> list = customerService.selectCustomerList(customer);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:customer:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:customer:add")
    @Log(title = "客户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Customer customer)
    {
        customer.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(customerService.insertCustomer(customer));
    }

    @RequiresPermissions("biz:customer:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("customer", customerService.selectCustomerById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:customer:edit")
    @Log(title = "客户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Customer customer)
    {
        customer.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(customerService.updateCustomer(customer));
    }

    @RequiresPermissions("biz:customer:remove")
    @Log(title = "客户", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(customerService.deleteCustomerByIds(ids));
    }
}
