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
import com.ruoyi.business.domain.Sales;
import com.ruoyi.business.service.ISalesService;

/**
 * 销售单 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/sales")
public class SalesController extends BaseController
{
    private String prefix = "biz/sales";

    @Autowired
    private ISalesService salesService;

    @RequiresPermissions("biz:sales:view")
    @GetMapping()
    public String sales()
    {
        return prefix + "/sales";
    }

    @RequiresPermissions("biz:sales:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Sales sales)
    {
        startPage();
        List<Sales> list = salesService.selectSalesList(sales);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:sales:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:sales:add")
    @Log(title = "销售单", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Sales sales)
    {
        if (!salesService.checkStockEnough(sales))
        {
            return error("库存不足，无法出库，请先采购入库");
        }
        sales.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(salesService.insertSales(sales));
    }

    @RequiresPermissions("biz:sales:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("sales", salesService.selectSalesById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:sales:edit")
    @Log(title = "销售单", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Sales sales)
    {
        sales.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(salesService.updateSales(sales));
    }

    @RequiresPermissions("biz:sales:remove")
    @Log(title = "销售单", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(salesService.deleteSalesByIds(ids));
    }
}
