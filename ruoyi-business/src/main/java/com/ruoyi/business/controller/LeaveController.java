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
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.service.ILeaveService;

/**
 * 请假 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/leave")
public class LeaveController extends BaseController
{
    private String prefix = "biz/leave";

    @Autowired
    private ILeaveService leaveService;

    @RequiresPermissions("biz:leave:view")
    @GetMapping()
    public String leave()
    {
        return prefix + "/leave";
    }

    @RequiresPermissions("biz:leave:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Leave leave)
    {
        startPage();
        List<Leave> list = leaveService.selectLeaveList(leave);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:leave:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:leave:add")
    @Log(title = "请假", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Leave leave)
    {
        leave.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(leaveService.insertLeave(leave));
    }

    @RequiresPermissions("biz:leave:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("leave", leaveService.selectLeaveById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:leave:edit")
    @Log(title = "请假", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Leave leave)
    {
        leave.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(leaveService.updateLeave(leave));
    }

    /**
     * 审批请假
     */
    @RequiresPermissions("biz:leave:audit")
    @Log(title = "请假审批", businessType = BusinessType.UPDATE)
    @PostMapping("/audit")
    @ResponseBody
    public AjaxResult auditSave(Long id, String status, String auditRemark)
    {
        if (!"1".equals(status) && !"2".equals(status))
        {
            return error("审批状态不合法");
        }
        return toAjax(leaveService.auditLeave(id, status, auditRemark));
    }

    @RequiresPermissions("biz:leave:export")
    @Log(title = "请假", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Leave leave)
    {
        List<Leave> list = leaveService.selectLeaveList(leave);
        ExcelUtil<Leave> util = new ExcelUtil<Leave>(Leave.class);
        return util.exportExcel(list, "请假数据");
    }

    @RequiresPermissions("biz:leave:remove")
    @Log(title = "请假", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(leaveService.deleteLeaveByIds(ids));
    }
}
