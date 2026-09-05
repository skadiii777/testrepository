package com.enterprise.module.biz.controller.admin.leave;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.leave.vo.leave.*;
import com.enterprise.module.biz.dal.dataobject.leave.LeaveDO;
import com.enterprise.module.biz.service.leave.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 请假")
@RestController
@RequestMapping("/biz/leave")
@Validated
public class LeaveController {

    @Resource
    private LeaveService leaveService;

    @PostMapping("/create")
    @Operation(summary = "创建请假")
    @PreAuthorize("@ss.hasPermission('biz:leave:create')")
    public CommonResult<Long> createLeave(@Valid @RequestBody LeaveSaveReqVO createReqVO) {
        return success(leaveService.createLeave(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新请假")
    @PreAuthorize("@ss.hasPermission('biz:leave:update')")
    public CommonResult<Boolean> updateLeave(@Valid @RequestBody LeaveSaveReqVO updateReqVO) {
        leaveService.updateLeave(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除请假")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:leave:delete')")
    public CommonResult<Boolean> deleteLeave(@RequestParam("id") Long id) {
        leaveService.deleteLeave(id);
        return success(true);
    }

    @PostMapping("/audit")
    @Operation(summary = "审批请假")
    @PreAuthorize("@ss.hasPermission('biz:leave:audit')")
    public CommonResult<Boolean> auditLeave(@RequestParam("id") Long id,
                                            @RequestParam("status") String status,
                                            @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        leaveService.auditLeave(id, status, auditRemark);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得请假")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:leave:query')")
    public CommonResult<LeaveRespVO> getLeave(@RequestParam("id") Long id) {
        LeaveDO leave = leaveService.getLeave(id);
        return success(BeanUtils.toBean(leave, LeaveRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得请假分页")
    @PreAuthorize("@ss.hasPermission('biz:leave:query')")
    public CommonResult<PageResult<LeaveRespVO>> getLeavePage(@Valid LeavePageReqVO pageReqVO) {
        PageResult<LeaveDO> pageResult = leaveService.getLeavePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, LeaveRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出请假 Excel")
    @PreAuthorize("@ss.hasPermission('biz:leave:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLeaveExcel(@Valid LeavePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<LeaveRespVO> list = BeanUtils.toBean(leaveService.getLeavePage(pageReqVO).getList(), LeaveRespVO.class);
        ExcelUtils.write(response, "请假.xls", "数据", LeaveRespVO.class, list);
    }
}