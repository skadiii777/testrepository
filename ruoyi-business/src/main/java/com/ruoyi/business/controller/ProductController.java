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
import com.ruoyi.business.domain.Product;
import com.ruoyi.business.service.IProductService;

/**
 * 产品 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/product")
public class ProductController extends BaseController
{
    private String prefix = "biz/product";

    @Autowired
    private IProductService productService;

    @RequiresPermissions("biz:product:view")
    @GetMapping()
    public String product()
    {
        return prefix + "/product";
    }

    @RequiresPermissions("biz:product:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Product product)
    {
        startPage();
        List<Product> list = productService.selectProductList(product);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:product:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:product:add")
    @Log(title = "产品", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Product product)
    {
        product.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(productService.insertProduct(product));
    }

    @RequiresPermissions("biz:product:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("product", productService.selectProductById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:product:edit")
    @Log(title = "产品", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Product product)
    {
        product.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(productService.updateProduct(product));
    }

    @RequiresPermissions("biz:product:export")
    @Log(title = "产品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Product product)
    {
        List<Product> list = productService.selectProductList(product);
        ExcelUtil<Product> util = new ExcelUtil<Product>(Product.class);
        return util.exportExcel(list, "产品数据");
    }

    @RequiresPermissions("biz:product:remove")
    @Log(title = "产品", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(productService.deleteProductByIds(ids));
    }
}
