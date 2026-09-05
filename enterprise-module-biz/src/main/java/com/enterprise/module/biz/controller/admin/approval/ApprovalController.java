package com.enterprise.module.biz.controller.admin.approval;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeavePageReqVO;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveRespVO;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpensePageReqVO;
import com.enterprise.module.biz.controller.admin.expense.vo.expense.ExpenseRespVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionRespVO;
import com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.dal.mysql.expense.ExpenseMapper;
import com.enterprise.module.biz.dal.mysql.leave.LeaveMapper;
import com.enterprise.module.biz.dal.mysql.correction.AttendanceCorrectionMapper;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import com.enterprise.module.biz.service.expense.ExpenseService;
import com.enterprise.module.biz.service.leave.LeaveService;
import com.enterprise.module.biz.service.correction.AttendanceCorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 审批中心")
@RestController
@RequestMapping("/biz/approval")
@Validated
public class ApprovalController {

    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private ExpenseMapper expenseMapper;
    @Resource
    private AttendanceCorrectionMapper correctionMapper;
    @Resource
    private LeaveService leaveService;
    @Resource
    private ExpenseService expenseService;
    @Resource
    private AttendanceCorrectionService correctionService;

    @PostMapping("/pending")
    @Operation(summary = "待办数量角标")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<Map<String, Object>> pending() {
        Map<String, Object> data = new HashMap<>();
        data.put("leaveCount", leaveMapper.selectCount(new QueryWrapper<LeaveDO>().eq("status", "0")));
        data.put("expenseCount", expenseMapper.selectCount(new QueryWrapper<ExpenseDO>().eq("status", "0")));
        data.put("correctionCount", correctionMapper.selectCount(new QueryWrapper<AttendanceCorrectionDO>().eq("status", "0")));
        return success(data);
    }

    @GetMapping("/leave-page")
    @Operation(summary = "待审批请假分页")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<PageResult<LeaveRespVO>> getLeavePage(LeavePageReqVO pageReqVO) {
        pageReqVO.setStatus("0");
        PageResult<LeaveDO> pageResult = leaveService.getLeavePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, LeaveRespVO.class));
    }

    @PostMapping("/leave-audit")
    @Operation(summary = "审批请假")
    @PreAuthorize("@ss.hasPermission('biz:approval:audit')")
    public CommonResult<Boolean> auditLeave(@RequestParam("id") Long id,
                                            @RequestParam("status") String status,
                                            @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        leaveService.auditLeave(id, status, auditRemark);
        return success(true);
    }

    @GetMapping("/expense-page")
    @Operation(summary = "待审批报销分页")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<PageResult<ExpenseRespVO>> getExpensePage(ExpensePageReqVO pageReqVO) {
        pageReqVO.setStatus("0");
        PageResult<ExpenseDO> pageResult = expenseService.getExpensePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExpenseRespVO.class));
    }

    @GetMapping("/correction-page")
    @Operation(summary = "待审批补卡分页")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<PageResult<AttendanceCorrectionRespVO>> getCorrectionPage(
            AttendanceCorrectionPageReqVO pageReqVO) {
        pageReqVO.setStatus("0");
        PageResult<AttendanceCorrectionDO> pageResult = correctionService.getCorrectionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AttendanceCorrectionRespVO.class));
    }

    @PostMapping("/correction-audit")
    @Operation(summary = "审批补卡（通过后自动回写考勤）")
    @PreAuthorize("@ss.hasPermission('biz:approval:audit')")
    public CommonResult<Boolean> auditCorrection(@RequestParam("id") Long id,
                                                 @RequestParam("status") String status,
                                                 @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        correctionService.auditCorrection(id, status, auditRemark, getLoginUserId());
        return success(true);
    }

    @PostMapping("/expense-audit")
    @Operation(summary = "审批报销")
    @PreAuthorize("@ss.hasPermission('biz:approval:audit')")
    public CommonResult<Boolean> auditExpense(@RequestParam("id") Long id,
                                              @RequestParam("status") String status,
                                              @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        expenseService.auditExpense(id, status, auditRemark, getLoginUserId());
        return success(true);
    }
}