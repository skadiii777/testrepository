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
import com.ruoyi.business.domain.Stock;
import com.ruoyi.business.service.IStockService;

/**
 * 库存 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/stock")
public class StockController extends BaseController
{
    private String prefix = "biz/stock";

    @Autowired
    private IStockService stockService;

    @RequiresPermissions("biz:stock:view")
    @GetMapping()
    public String stock()
    {
        return prefix + "/stock";
    }

    @RequiresPermissions("biz:stock:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Stock stock)
    {
        startPage();
        List<Stock> list = stockService.selectStockList(stock);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:stock:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:stock:add")
    @Log(title = "库存", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Stock stock)
    {
        stock.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(stockService.insertStock(stock));
    }

    @RequiresPermissions("biz:stock:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("stock", stockService.selectStockById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:stock:edit")
    @Log(title = "库存", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Stock stock)
    {
        stock.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(stockService.updateStock(stock));
    }

    @RequiresPermissions("biz:stock:remove")
    @Log(title = "库存", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(stockService.deleteStockByIds(ids));
    }
}
