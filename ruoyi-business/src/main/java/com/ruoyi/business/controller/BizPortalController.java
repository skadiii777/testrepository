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
import com.ruoyi.business.domain.Attendance;
import com.ruoyi.business.domain.Leave;
import com.ruoyi.business.domain.Report;
import com.ruoyi.business.service.IAttendanceService;
import com.ruoyi.business.service.ILeaveService;
import com.ruoyi.business.service.ILeaveQuotaService;
import com.ruoyi.business.service.IReportService;
import com.ruoyi.business.service.IExpenseService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.ShiroUtils;

/**
 * 员工工作台：打卡、请销假、业务汇报
 *
 * @author biz
 */
@Controller
@RequestMapping("/portal")
public class BizPortalController extends BaseController
{
    private String prefix = "portal";

    /** 上班时间，晚于此打卡记迟到 */
    private static final String WORK_START = "09:00";
    /** 下班时间，早于此打卡记早退 */
    private static final String WORK_END = "18:00";

    @Autowired
    private IAttendanceService attendanceService;
    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private ILeaveQuotaService quotaService;
    @Autowired
    private IReportService reportService;

    @Autowired
    private IExpenseService expenseService;

    /**
     * 工作台首页：今日打卡状态
     */
    @RequiresPermissions("portal:index:view")
    @GetMapping()
    public String index(ModelMap mmap)
    {
        com.ruoyi.business.domain.LeaveQuota quotaQuery = new com.ruoyi.business.domain.LeaveQuota();
        quotaQuery.setEmpName(displayName());
        quotaQuery.setYear(String.valueOf(java.time.Year.now().getValue()));
        mmap.put("quotas", quotaService.selectLeaveQuotaList(quotaQuery));
        Attendance query = new Attendance();
        query.setEmpName(displayName());
        query.setWorkDate(today());
        List<Attendance> list = attendanceService.selectAttendanceList(query);
        mmap.put("today", today());
        mmap.put("workStart", WORK_START);
        mmap.put("workEnd", WORK_END);
        mmap.put("attendance", list.isEmpty() ? null : list.get(0));
        mmap.put("userName", displayName());
        return prefix + "/index";
    }

    /**
     * 打卡（type=in 上班 / out 下班）
     */
    @RequiresPermissions("portal:punch:add")
    @Log(title = "员工打卡", businessType = BusinessType.INSERT)
    @PostMapping("/punch")
    @ResponseBody
    public AjaxResult punch(String type)
    {
        String now = nowTime();
        Attendance query = new Attendance();
        query.setEmpName(displayName());
        query.setWorkDate(today());
        List<Attendance> list = attendanceService.selectAttendanceList(query);
        Attendance today = list.isEmpty() ? null : list.get(0);

        if ("in".equals(type))
        {
            if (today != null && today.getCheckIn() != null)
            {
                return error("今日已完成上班打卡（" + today.getCheckIn() + "），无需重复打卡");
            }
            Attendance record = new Attendance();
            record.setEmpName(displayName());
            record.setWorkDate(today());
            record.setCheckIn(now);
            record.setStatus(now.compareTo(WORK_START) > 0 ? "1" : "0");
            attendanceService.insertAttendance(record);
            return success(now.compareTo(WORK_START) > 0 ? "上班打卡成功（" + now + "），迟到" : "上班打卡成功（" + now + "）");
        }
        else if ("out".equals(type))
        {
            if (today == null)
            {
                Attendance record = new Attendance();
                record.setEmpName(displayName());
                record.setWorkDate(today());
                record.setCheckOut(now);
                record.setStatus("3");
                attendanceService.insertAttendance(record);
                return success("下班打卡成功（" + now + "），注意：今日无上班打卡记录");
            }
            if (today.getCheckOut() != null)
            {
                return error("今日已完成下班打卡（" + today.getCheckOut() + "），无需重复打卡");
            }
            Attendance record = new Attendance();
            record.setId(today.getId());
            record.setCheckOut(now);
            if ("1".equals(today.getStatus()))
            {
                record.setStatus(today.getStatus());
            }
            else
            {
                record.setStatus(now.compareTo(WORK_END) < 0 ? "2" : "0");
            }
            attendanceService.updateAttendance(record);
            boolean early = now.compareTo(WORK_END) < 0;
            return success("下班打卡成功（" + now + "）" + (early ? "，早退" : ""));
        }
        return error("打卡类型不合法");
    }

    /**
     * 我的请假列表页
     */
    @RequiresPermissions("portal:leave:view")
    @GetMapping("/leave")
    public String leavePage()
    {
        return prefix + "/leave";
    }

    /**
     * 我的请假数据（仅本人）
     */
    @RequiresPermissions("portal:leave:view")
    @PostMapping("/leave/list")
    @ResponseBody
    public TableDataInfo leaveList(Leave leave)
    {
        startPage();
        leave.getParams().put("createBy", ShiroUtils.getLoginName());
        List<Leave> list = leaveService.selectLeaveList(leave);
        return getDataTable(list);
    }

    /**
     * 新增请假页
     */
    @RequiresPermissions("portal:leave:add")
    @GetMapping("/leave/add")
    public String leaveAdd()
    {
        return prefix + "/leave_add";
    }

    /**
     * 提交请假
     */
    @RequiresPermissions("portal:leave:add")
    @Log(title = "员工请假", businessType = BusinessType.INSERT)
    @PostMapping("/leave/add")
    @ResponseBody
    public AjaxResult leaveSave(Leave leave)
    {
        // 提交时余额预校验：仅当配置了配额才校验，避免审批通过时才发现余额不足
        String year = leave.getStartDate() != null && leave.getStartDate().length() >= 4
                ? leave.getStartDate().substring(0, 4) : String.valueOf(java.time.Year.now().getValue());
        java.math.BigDecimal days = leave.getDays() == null ? java.math.BigDecimal.ZERO : leave.getDays();
        java.math.BigDecimal remain = quotaService.findRemainDays(displayName(), leave.getLeaveType(), year);
        if (days.compareTo(java.math.BigDecimal.ZERO) > 0 && remain.compareTo(days) < 0)
        {
            return error(String.format("提交失败：您%d年该假期类型余额不足，剩余%s天，本次申请%s天",
                    Integer.parseInt(year), remain, days));
        }
        leave.setEmpName(displayName());
        leave.setStatus("0");
        leave.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(leaveService.insertLeave(leave));
    }

    /**
     * 销假：已通过的请假可申请销假（提前返岗）
     */
    @RequiresPermissions("portal:leave:add")
    @Log(title = "员工销假", businessType = BusinessType.UPDATE)
    @PostMapping("/leave/cancel")
    @ResponseBody
    public AjaxResult leaveCancel(Long id)
    {
        Leave leave = leaveService.selectLeaveById(id);
        if (leave == null || !ShiroUtils.getLoginName().equals(leave.getCreateBy()))
        {
            return error("仅能操作本人提交的请假");
        }
        if (!"1".equals(leave.getStatus()))
        {
            return error("仅已通过的请假可以销假");
        }
        // 返还假期余额（有配额记录才返还，失败不阻断销假流程）
        String year = leave.getStartDate() != null && leave.getStartDate().length() >= 4
                ? leave.getStartDate().substring(0, 4) : String.valueOf(java.time.Year.now().getValue());
        quotaService.refundUsedDays(leave.getEmpName(), leave.getLeaveType(), year, leave.getDays());
        Leave update = new Leave();
        update.setId(id);
        update.setStatus("3");
        update.setUpdateBy(ShiroUtils.getLoginName());
        update.setRemark("员工申请销假（提前返岗）");
        return toAjax(leaveService.updateLeave(update));
    }

    /**
     * 我的业务汇报页
     */
    @RequiresPermissions("portal:report:view")
    @GetMapping("/report")
    public String reportPage()
    {
        return prefix + "/report";
    }

    /**
     * 我的业务汇报数据（仅本人）
     */
    @RequiresPermissions("portal:report:view")
    @PostMapping("/report/list")
    @ResponseBody
    public TableDataInfo reportList(Report report)
    {
        startPage();
        report.getParams().put("createBy", ShiroUtils.getLoginName());
        List<Report> list = reportService.selectReportList(report);
        return getDataTable(list);
    }

    /**
     * 汇报详情
     */
    @RequiresPermissions("portal:report:view")
    @GetMapping("/report/view/{id}")
    public String reportView(@PathVariable("id") Long id, ModelMap mmap)
    {
        mmap.put("report", reportService.selectReportById(id));
        return prefix + "/report_view";
    }

    /**
     * 新增汇报页
     */
    @RequiresPermissions("portal:report:add")
    @GetMapping("/report/add")
    public String reportAdd()
    {
        return prefix + "/report_add";
    }

    /**
     * 提交汇报
     */
    @RequiresPermissions("portal:report:add")
    @Log(title = "业务汇报", businessType = BusinessType.INSERT)
    @PostMapping("/report/add")
    @ResponseBody
    public AjaxResult reportSave(Report report)
    {
        report.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(reportService.insertReport(report));
    }

    /**
     * 删除本人汇报
     */
    @RequiresPermissions("portal:report:add")
    @Log(title = "业务汇报", businessType = BusinessType.DELETE)
    @PostMapping("/report/remove")
    @ResponseBody
    public AjaxResult reportRemove(Long id)
    {
        Report report = reportService.selectReportById(id);
        if (report == null || !ShiroUtils.getLoginName().equals(report.getCreateBy()))
        {
            return error("仅能删除本人提交的汇报");
        }
        return toAjax(reportService.deleteReportByIds(String.valueOf(id)));
    }

    /**
     * 我的报销列表页
     */
    @RequiresPermissions("portal:expense:view")
    @GetMapping("/expense")
    public String expensePage()
    {
        return prefix + "/expense";
    }

    /**
     * 我的报销数据（仅本人）
     */
    @RequiresPermissions("portal:expense:view")
    @PostMapping("/expense/list")
    @ResponseBody
    public TableDataInfo expenseList(com.ruoyi.business.domain.Expense expense)
    {
        startPage();
        expense.getParams().put("createBy", ShiroUtils.getLoginName());
        List<com.ruoyi.business.domain.Expense> list = expenseService.selectExpenseList(expense);
        return getDataTable(list);
    }

    /**
     * 提交报销
     */
    @RequiresPermissions("portal:expense:add")
    @Log(title = "员工报销", businessType = BusinessType.INSERT)
    @PostMapping("/expense/add")
    @ResponseBody
    public AjaxResult expenseSave(com.ruoyi.business.domain.Expense expense)
    {
        if (expense.getAmount() == null || expense.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0)
        {
            return error("报销金额必须大于0");
        }
        expense.setEmpName(displayName());
        expense.setStatus("0");
        expense.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(expenseService.insertExpense(expense));
    }

    /**
     * 撤回待审批的报销
     */
    @RequiresPermissions("portal:expense:add")
    @Log(title = "员工报销", businessType = BusinessType.DELETE)
    @PostMapping("/expense/remove")
    @ResponseBody
    public AjaxResult expenseRemove(Long id)
    {
        com.ruoyi.business.domain.Expense expense = expenseService.selectExpenseById(id);
        if (expense == null || !ShiroUtils.getLoginName().equals(expense.getCreateBy()))
        {
            return error("仅能操作本人提交的报销");
        }
        if (!"0".equals(expense.getStatus()))
        {
            return error("已审批的报销不能撤回");
        }
        return toAjax(expenseService.deleteExpenseByIds(String.valueOf(id)));
    }

    private String displayName()
    {
        return ShiroUtils.getSysUser().getUserName();
    }

    private String today()
    {
        return java.time.LocalDate.now().toString();
    }

    private String nowTime()
    {
        return java.time.LocalTime.now().withNano(0).toString().substring(0, 5);
    }
}
