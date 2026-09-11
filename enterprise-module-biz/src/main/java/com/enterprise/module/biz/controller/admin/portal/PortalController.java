package com.enterprise.module.biz.controller.admin.portal;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.security.core.util.SecurityFrameworkUtils;
import com.enterprise.module.system.api.user.AdminUserApi;
import com.enterprise.module.system.api.user.dto.AdminUserRespDTO;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionRespVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import com.enterprise.module.biz.service.correction.AttendanceCorrectionService;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseRespVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseSaveReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveRespVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportPageReqVO;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportRespVO;
import com.enterprise.module.biz.controller.admin.report.vo.report.ReportSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.dal.dataobject.report.ReportDO;
import com.enterprise.module.biz.service.attendance.AttendanceService;
import com.enterprise.module.biz.service.expense.ExpenseService;
import com.enterprise.module.biz.service.leave.LeaveService;
import com.enterprise.module.biz.service.quota.LeaveQuotaService;
import com.enterprise.module.biz.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

@Tag(name = "管理后台 - 员工工作台")
@RestController
@RequestMapping("/portal")
@Validated
public class PortalController {

    private static final String WORK_START = "09:00";
    private static final String WORK_END = "18:00";

    @Resource
    private AttendanceService attendanceService;
    @Resource
    private LeaveService leaveService;
    @Resource
    private LeaveQuotaService quotaService;
    @Resource
    private ReportService reportService;
    @Resource
    private ExpenseService expenseService;
    @Resource
    private AttendanceCorrectionService correctionService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource private com.enterprise.module.biz.service.support.BizReferenceService references;

    private Integer otMinutes(String now, String workEnd) {
        int nowMin = Integer.parseInt(now.substring(0, 2)) * 60 + Integer.parseInt(now.substring(3));
        int endMin = Integer.parseInt(workEnd.substring(0, 2)) * 60 + Integer.parseInt(workEnd.substring(3));
        return Math.max(0, nowMin - endMin);
    }

    private String displayName() {
        AdminUserRespDTO user = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId());
        return user == null ? "未知" : user.getNickname();
    }

    private Long loginUserId() {
        return SecurityFrameworkUtils.getLoginUserId();
    }

    @GetMapping("/index-data")
    @Operation(summary = "工作台首页数据：今日打卡 + 假期余额")
    @PreAuthorize("@ss.hasPermission('portal:index:query')")
    public CommonResult<Map<String, Object>> indexData() {
        Map<String, Object> data = new HashMap<>();
        data.put("today", LocalDate.now().toString());
        data.put("workStart", WORK_START);
        data.put("workEnd", WORK_END);
        AttendanceDO today = attendanceService.getTodayAttendance(displayName(), LocalDate.now().toString());
        data.put("checkIn", today == null ? null : today.getCheckIn());
        data.put("checkOut", today == null ? null : today.getCheckOut());
        data.put("status", today == null ? null : today.getStatus());
        data.put("monthOvertimeMinutes", attendanceService.getMonthOvertimeMinutes(
                displayName(), String.valueOf(LocalDate.now().getYear())
                        + String.format("%02d", LocalDate.now().getMonthValue())));
        Map<String, Object> quotaMap = new LinkedHashMap<>();
        var linkedEmployee = references.findEmployeeForUser(loginUserId());
        for (String type : linkedEmployee == null ? java.util.Collections.<String>emptyList() : Arrays.asList("1", "2", "3", "4")) {
            BigDecimal remain = quotaService.findRemainDays(linkedEmployee.getId(), type, String.valueOf(LocalDate.now().getYear()));
            if (remain.compareTo(new BigDecimal("99999")) < 0) {
                quotaMap.put(type, remain);
            }
        }
        data.put("quotas", quotaMap);
        return success(data);
    }

    @PostMapping("/punch")
    @Operation(summary = "打卡（type: in 上班 / out 下班）")
    @PreAuthorize("@ss.hasPermission('portal:punch:add')")
    public CommonResult<String> punch(@RequestParam("type") String type) {
        String now = LocalTime.now().withNano(0).toString().substring(0, 5);
        String today = LocalDate.now().toString();
        String name = displayName();
        AttendanceDO record = attendanceService.getTodayAttendance(name, today);
        if ("in".equals(type)) {
            if (record != null && record.getCheckIn() != null) {
                throw exception(PUNCH_DUPLICATE);
            }
            AttendanceSaveReqVO save = new AttendanceSaveReqVO();
            save.setEmpName(name);
            save.setWorkDate(today);
            save.setCheckIn(now);
            save.setStatus(now.compareTo(WORK_START) > 0 ? "1" : "0");
            if (record == null) {
                attendanceService.createAttendance(save);
            } else {
                save.setId(record.getId());
                attendanceService.updateAttendance(save);
            }
            return success("上班打卡成功（" + now + "）" + (now.compareTo(WORK_START) > 0 ? "，迟到" : ""));
        } else if ("out".equals(type)) {
            if (record != null && record.getCheckOut() != null) {
                throw exception(PUNCH_DUPLICATE);
            }
            AttendanceSaveReqVO save = new AttendanceSaveReqVO();
            save.setEmpName(name);
            save.setWorkDate(today);
            save.setCheckOut(now);
            // 晚于 18:00 下班自动累计加班分钟
            if (now.compareTo(WORK_END) > 0) {
                save.setOvertimeMinutes(otMinutes(now, WORK_END));
            }
            if (record == null) {
                save.setStatus("3");
                attendanceService.createAttendance(save);
                return success("下班打卡成功（" + now + "），注意：今日无上班打卡记录");
            }
            // 已有记录：必定更新该记录（迟到状态保持为迟到，其余按下班时间判定早退）
            save.setId(record.getId());
            if (!"1".equals(record.getStatus())) {
                save.setStatus(now.compareTo(WORK_END) < 0 ? "2" : "0");
            }
            attendanceService.updateAttendance(save);
            return success("下班打卡成功（" + now + "）"
                    + (now.compareTo(WORK_END) < 0 ? "，早退" : (save.getOvertimeMinutes() != null
                        ? "，今日加班 " + save.getOvertimeMinutes() + " 分钟" : "")));
        }
        throw exception(PUNCH_TYPE_INVALID);
    }

    @GetMapping("/leave-page")
    @Operation(summary = "我的请假分页")
    @PreAuthorize("@ss.hasPermission('portal:leave:query')")
    public CommonResult<PageResult<LeaveRespVO>> getLeavePage(LeavePageReqVO pageReqVO) {
        return success(BeanUtils.toBean(leaveService.getLeavePageSelf(pageReqVO, loginUserId()), LeaveRespVO.class));
    }

    @PostMapping("/leave-submit")
    @Operation(summary = "提交请假")
    @PreAuthorize("@ss.hasPermission('portal:leave:add')")
    public CommonResult<Long> submitLeave(@Valid @RequestBody LeaveSaveReqVO createReqVO) {
        String year = createReqVO.getStartDate() != null && createReqVO.getStartDate().length() >= 4
                ? createReqVO.getStartDate().substring(0, 4) : String.valueOf(LocalDate.now().getYear());
        BigDecimal days = createReqVO.getDays() == null ? BigDecimal.ZERO : createReqVO.getDays();
        BigDecimal remain = quotaService.findRemainDays(references.employeeForUser(loginUserId()).getId(), createReqVO.getLeaveType(), year);
        if (days.compareTo(BigDecimal.ZERO) > 0 && remain.compareTo(days) < 0) {
            throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }
        var employee = references.employeeForUser(loginUserId());
        createReqVO.setEmployeeId(employee.getId());
        createReqVO.setEmpName(employee.getEmpName());
        createReqVO.setStatus("0");
        return success(leaveService.createLeave(createReqVO));
    }

    @PostMapping("/leave-cancel")
    @Operation(summary = "销假")
    @PreAuthorize("@ss.hasPermission('portal:leave:add')")
    public CommonResult<Boolean> cancelLeave(@RequestParam("id") Long id) {
        leaveService.cancelLeave(id, loginUserId());
        return success(true);
    }

    @GetMapping("/report-page")
    @Operation(summary = "我的汇报分页")
    @PreAuthorize("@ss.hasPermission('portal:report:query')")
    public CommonResult<PageResult<ReportRespVO>> getReportPage(ReportPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(reportService.getReportPageSelf(pageReqVO, loginUserId()), ReportRespVO.class));
    }

    @PostMapping("/report-submit")
    @Operation(summary = "提交汇报")
    @PreAuthorize("@ss.hasPermission('portal:report:add')")
    public CommonResult<Long> submitReport(@Valid @RequestBody ReportSaveReqVO createReqVO) {
        return success(reportService.createReport(createReqVO));
    }

    @DeleteMapping("/report-delete")
    @Operation(summary = "删除本人汇报")
    @PreAuthorize("@ss.hasPermission('portal:report:add')")
    public CommonResult<Boolean> deleteReport(@RequestParam("id") Long id) {
        ReportDO report = reportService.getReport(id);
        if (report == null || !String.valueOf(loginUserId()).equals(report.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        reportService.deleteReport(id);
        return success(true);
    }

    @GetMapping("/correction-page")
    @Operation(summary = "我的补卡分页")
    @PreAuthorize("@ss.hasPermission('portal:correction:query')")
    public CommonResult<PageResult<AttendanceCorrectionRespVO>> getCorrectionPage(
            AttendanceCorrectionPageReqVO pageReqVO) {
        return success(BeanUtils.toBean(
                correctionService.getCorrectionPageSelf(pageReqVO, loginUserId()), AttendanceCorrectionRespVO.class));
    }

    @PostMapping("/correction-submit")
    @Operation(summary = "提交补卡申请")
    @PreAuthorize("@ss.hasPermission('portal:correction:add')")
    public CommonResult<Long> submitCorrection(@Valid @RequestBody AttendanceCorrectionSaveReqVO createReqVO) {
        createReqVO.setEmpName(displayName());
        return success(correctionService.createCorrection(createReqVO));
    }

    @DeleteMapping("/correction-withdraw")
    @Operation(summary = "撤回待审批补卡")
    @PreAuthorize("@ss.hasPermission('portal:correction:add')")
    public CommonResult<Boolean> withdrawCorrection(@RequestParam("id") Long id) {
        AttendanceCorrectionDO correction = correctionService.getCorrection(id);
        if (correction == null || !String.valueOf(loginUserId()).equals(correction.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        if (!"0".equals(correction.getStatus())) {
            throw exception(CORRECTION_ALREADY_AUDITED);
        }
        correctionService.deleteCorrection(id);
        return success(true);
    }

    @GetMapping("/expense-page")
    @Operation(summary = "我的报销分页")
    @PreAuthorize("@ss.hasPermission('portal:expense:query')")
    public CommonResult<PageResult<ExpenseRespVO>> getExpensePage(ExpensePageReqVO pageReqVO) {
        return success(BeanUtils.toBean(expenseService.getExpensePageSelf(pageReqVO, loginUserId()), ExpenseRespVO.class));
    }

    @PostMapping("/expense-submit")
    @Operation(summary = "提交报销")
    @PreAuthorize("@ss.hasPermission('portal:expense:add')")
    public CommonResult<Long> submitExpense(@Valid @RequestBody ExpenseSaveReqVO createReqVO) {
        if (createReqVO.getAmount() == null || createReqVO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(EXPENSE_AMOUNT_INVALID);
        }
        createReqVO.setEmpName(displayName());
        createReqVO.setStatus("0");
        return success(expenseService.createExpense(createReqVO));
    }

    @DeleteMapping("/expense-withdraw")
    @Operation(summary = "撤回待审批报销")
    @PreAuthorize("@ss.hasPermission('portal:expense:add')")
    public CommonResult<Boolean> withdrawExpense(@RequestParam("id") Long id) {
        ExpenseDO expense = expenseService.getExpense(id);
        if (expense == null || !String.valueOf(loginUserId()).equals(expense.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        if (!"0".equals(expense.getStatus())) {
            throw exception(EXPENSE_ALREADY_AUDITED);
        }
        expenseService.deleteExpense(id);
        return success(true);
    }
}