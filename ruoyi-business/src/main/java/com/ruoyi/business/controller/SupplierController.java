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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.ShiroUtils;
import com.ruoyi.business.domain.Supplier;
import com.ruoyi.business.service.ISupplierService;

/**
 * 供应商 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/supplier")
public class SupplierController extends BaseController
{
    private String prefix = "biz/supplier";

    @Autowired
    private ISupplierService supplierService;

    @RequiresPermissions("biz:supplier:view")
    @GetMapping()
    public String supplier()
    {
        return prefix + "/supplier";
    }

    @RequiresPermissions("biz:supplier:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Supplier supplier)
    {
        startPage();
        List<Supplier> list = supplierService.selectSupplierList(supplier);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:supplier:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:supplier:add")
    @Log(title = "供应商", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Supplier supplier)
    {
        supplier.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(supplierService.insertSupplier(supplier));
    }

    @RequiresPermissions("biz:supplier:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("supplier", supplierService.selectSupplierById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:supplier:edit")
    @Log(title = "供应商", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Supplier supplier)
    {
        supplier.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(supplierService.updateSupplier(supplier));
    }

    @RequiresPermissions("biz:supplier:export")
    @Log(title = "供应商", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Supplier supplier)
    {
        List<Supplier> list = supplierService.selectSupplierList(supplier);
        ExcelUtil<Supplier> util = new ExcelUtil<Supplier>(Supplier.class);
        return util.exportExcel(list, "供应商数据");
    }

    @RequiresPermissions("biz:supplier:remove")
    @Log(title = "供应商", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(supplierService.deleteSupplierByIds(ids));
    }
}
