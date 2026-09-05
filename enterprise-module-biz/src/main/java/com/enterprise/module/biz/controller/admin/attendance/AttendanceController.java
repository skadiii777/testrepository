package com.enterprise.module.biz.controller.admin.attendance;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.attendance.vo.attendance.*;
import com.enterprise.module.biz.dal.dataobject.attendance.AttendanceDO;
import com.enterprise.module.biz.service.attendance.AttendanceService;
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

@Tag(name = "管理后台 - 考勤")
@RestController
@RequestMapping("/biz/attendance")
@Validated
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    @PostMapping("/create")
    @Operation(summary = "创建考勤")
    @PreAuthorize("@ss.hasPermission('biz:attendance:create')")
    public CommonResult<Long> createAttendance(@Valid @RequestBody AttendanceSaveReqVO createReqVO) {
        return success(attendanceService.createAttendance(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新考勤")
    @PreAuthorize("@ss.hasPermission('biz:attendance:update')")
    public CommonResult<Boolean> updateAttendance(@Valid @RequestBody AttendanceSaveReqVO updateReqVO) {
        attendanceService.updateAttendance(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除考勤")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:attendance:delete')")
    public CommonResult<Boolean> deleteAttendance(@RequestParam("id") Long id) {
        attendanceService.deleteAttendance(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得考勤")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:attendance:query')")
    public CommonResult<AttendanceRespVO> getAttendance(@RequestParam("id") Long id) {
        AttendanceDO attendance = attendanceService.getAttendance(id);
        return success(BeanUtils.toBean(attendance, AttendanceRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得考勤分页")
    @PreAuthorize("@ss.hasPermission('biz:attendance:query')")
    public CommonResult<PageResult<AttendanceRespVO>> getAttendancePage(@Valid AttendancePageReqVO pageReqVO) {
        PageResult<AttendanceDO> pageResult = attendanceService.getAttendancePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AttendanceRespVO.class));
    }

    @GetMapping("/summary")
    @Operation(summary = "考勤月报（按员工汇总出勤/迟到/早退/缺勤/加班）")
    @Parameter(name = "month", description = "月份（yyyy-MM）", required = true)
    @PreAuthorize("@ss.hasPermission('biz:attendance:query')")
    public CommonResult<java.util.List<java.util.Map<String, Object>>> getMonthlySummary(
            @RequestParam(value = "month") String month) {
        return success(attendanceService.getMonthlySummary(month));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出考勤 Excel")
    @PreAuthorize("@ss.hasPermission('biz:attendance:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAttendanceExcel(@Valid AttendancePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<AttendanceRespVO> list = BeanUtils.toBean(attendanceService.getAttendancePage(pageReqVO).getList(), AttendanceRespVO.class);
        ExcelUtils.write(response, "考勤.xls", "数据", AttendanceRespVO.class, list);
    }
}