package com.enterprise.module.biz.controller.admin.report;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.report.vo.report.*;
import com.enterprise.module.biz.dal.dataobject.report.ReportDO;
import com.enterprise.module.biz.service.report.ReportService;
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

@Tag(name = "管理后台 - 业务汇报")
@RestController
@RequestMapping("/biz/report")
@Validated
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping("/create")
    @Operation(summary = "创建业务汇报")
    @PreAuthorize("@ss.hasPermission('biz:report:create')")
    public CommonResult<Long> createReport(@Valid @RequestBody ReportSaveReqVO createReqVO) {
        return success(reportService.createReport(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新业务汇报")
    @PreAuthorize("@ss.hasPermission('biz:report:update')")
    public CommonResult<Boolean> updateReport(@Valid @RequestBody ReportSaveReqVO updateReqVO) {
        reportService.updateReport(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除业务汇报")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:report:delete')")
    public CommonResult<Boolean> deleteReport(@RequestParam("id") Long id) {
        reportService.deleteReport(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得业务汇报")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:report:query')")
    public CommonResult<ReportRespVO> getReport(@RequestParam("id") Long id) {
        ReportDO report = reportService.getReport(id);
        return success(BeanUtils.toBean(report, ReportRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得业务汇报分页")
    @PreAuthorize("@ss.hasPermission('biz:report:query')")
    public CommonResult<PageResult<ReportRespVO>> getReportPage(@Valid ReportPageReqVO pageReqVO) {
        PageResult<ReportDO> pageResult = reportService.getReportPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ReportRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出业务汇报 Excel")
    @PreAuthorize("@ss.hasPermission('biz:report:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReportExcel(@Valid ReportPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<ReportRespVO> list = BeanUtils.toBean(reportService.getReportPage(pageReqVO).getList(), ReportRespVO.class);
        ExcelUtils.write(response, "业务汇报.xls", "数据", ReportRespVO.class, list);
    }
}