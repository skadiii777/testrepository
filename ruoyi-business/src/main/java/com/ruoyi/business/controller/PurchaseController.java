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
import com.ruoyi.business.domain.Purchase;
import com.ruoyi.business.service.IPurchaseService;

/**
 * 采购单 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/purchase")
public class PurchaseController extends BaseController
{
    private String prefix = "biz/purchase";

    @Autowired
    private IPurchaseService purchaseService;

    @RequiresPermissions("biz:purchase:view")
    @GetMapping()
    public String purchase()
    {
        return prefix + "/purchase";
    }

    @RequiresPermissions("biz:purchase:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Purchase purchase)
    {
        startPage();
        List<Purchase> list = purchaseService.selectPurchaseList(purchase);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:purchase:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:purchase:add")
    @Log(title = "采购单", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Purchase purchase)
    {
        purchase.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(purchaseService.insertPurchase(purchase));
    }

    @RequiresPermissions("biz:purchase:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("purchase", purchaseService.selectPurchaseById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:purchase:edit")
    @Log(title = "采购单", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Purchase purchase)
    {
        purchase.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(purchaseService.updatePurchase(purchase));
    }

    @RequiresPermissions("biz:purchase:remove")
    @Log(title = "采购单", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(purchaseService.deletePurchaseByIds(ids));
    }
}
