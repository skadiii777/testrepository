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
import com.ruoyi.business.domain.Attendance;
import com.ruoyi.business.service.IAttendanceService;

/**
 * 考勤 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/attendance")
public class AttendanceController extends BaseController
{
    private String prefix = "biz/attendance";

    @Autowired
    private IAttendanceService attendanceService;

    @RequiresPermissions("biz:attendance:view")
    @GetMapping()
    public String attendance()
    {
        return prefix + "/attendance";
    }

    @RequiresPermissions("biz:attendance:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Attendance attendance)
    {
        startPage();
        List<Attendance> list = attendanceService.selectAttendanceList(attendance);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:attendance:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    @RequiresPermissions("biz:attendance:add")
    @Log(title = "考勤", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Attendance attendance)
    {
        attendance.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(attendanceService.insertAttendance(attendance));
    }

    @RequiresPermissions("biz:attendance:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("attendance", attendanceService.selectAttendanceById(id));
        return prefix + "/edit";
    }

    @RequiresPermissions("biz:attendance:edit")
    @Log(title = "考勤", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Attendance attendance)
    {
        attendance.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(attendanceService.updateAttendance(attendance));
    }

    @RequiresPermissions("biz:attendance:export")
    @Log(title = "考勤", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Attendance attendance)
    {
        List<Attendance> list = attendanceService.selectAttendanceList(attendance);
        ExcelUtil<Attendance> util = new ExcelUtil<Attendance>(Attendance.class);
        return util.exportExcel(list, "考勤数据");
    }

    @RequiresPermissions("biz:attendance:remove")
    @Log(title = "考勤", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(attendanceService.deleteAttendanceByIds(ids));
    }
}
