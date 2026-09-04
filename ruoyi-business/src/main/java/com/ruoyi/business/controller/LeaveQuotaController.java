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
import com.ruoyi.business.domain.LeaveQuota;
import com.ruoyi.business.service.ILeaveQuotaService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 假期余额 信息操作处理
 *
 * @author biz
 */
@Controller
@RequestMapping("/biz/quota")
public class LeaveQuotaController extends BaseController
{
    private String prefix = "biz/quota";

    @Autowired
    private ILeaveQuotaService quotaService;

    @RequiresPermissions("biz:quota:view")
    @GetMapping()
    public String quota()
    {
        return prefix + "/quota";
    }

    @RequiresPermissions("biz:quota:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(LeaveQuota quota)
    {
        startPage();
        List<LeaveQuota> list = quotaService.selectLeaveQuotaList(quota);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:quota:export")
    @Log(title = "假期余额", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(LeaveQuota quota)
    {
        List<LeaveQuota> list = quotaService.selectLeaveQuotaList(quota);
        ExcelUtil<LeaveQuota> util = new ExcelUtil<LeaveQuota>(LeaveQuota.class);
        return util.exportExcel(list, "假期余额数据");
    }

    @RequiresPermissions("biz:quota:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:quota:add")
    @Log(title = "假期余额", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(LeaveQuota quota)
    {
        quota.setCreateBy(getLoginName());
        return toAjax(quotaService.insertLeaveQuota(quota));
    }

    @RequiresPermissions("biz:quota:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("quota", quotaService.selectLeaveQuotaById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:quota:edit")
    @Log(title = "假期余额", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(LeaveQuota quota)
    {
        quota.setUpdateBy(getLoginName());
        return toAjax(quotaService.updateLeaveQuota(quota));
    }

    @RequiresPermissions("biz:quota:remove")
    @Log(title = "假期余额", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(quotaService.deleteLeaveQuotaByIds(ids));
    }
}
