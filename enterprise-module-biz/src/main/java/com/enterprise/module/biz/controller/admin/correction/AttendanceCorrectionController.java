package com.enterprise.module.biz.controller.admin.correction;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionPageReqVO;
import com.enterprise.module.biz.controller.admin.correction.vo.correction.AttendanceCorrectionRespVO;
import com.enterprise.module.biz.dal.dataobject.correction.AttendanceCorrectionDO;
import com.enterprise.module.biz.service.correction.AttendanceCorrectionService;
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

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 补卡申请")
@RestController
@RequestMapping("/biz/correction")
@Validated
public class AttendanceCorrectionController {

    @Resource
    private AttendanceCorrectionService correctionService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除补卡申请")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:correction:delete')")
    public CommonResult<Boolean> deleteCorrection(@RequestParam("id") Long id) {
        correctionService.deleteCorrection(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得补卡申请")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:correction:query')")
    public CommonResult<AttendanceCorrectionRespVO> getCorrection(@RequestParam("id") Long id) {
        AttendanceCorrectionDO correction = correctionService.getCorrection(id);
        return success(BeanUtils.toBean(correction, AttendanceCorrectionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得补卡申请分页")
    @PreAuthorize("@ss.hasPermission('biz:correction:query')")
    public CommonResult<PageResult<AttendanceCorrectionRespVO>> getCorrectionPage(
            @Valid AttendanceCorrectionPageReqVO pageReqVO) {
        PageResult<AttendanceCorrectionDO> pageResult = correctionService.getCorrectionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AttendanceCorrectionRespVO.class));
    }

    @PostMapping("/audit")
    @Operation(summary = "审批补卡（通过后自动回写考勤）")
    @PreAuthorize("@ss.hasPermission('biz:correction:audit')")
    public CommonResult<Boolean> auditCorrection(@RequestParam("id") Long id,
                                                 @RequestParam("status") String status,
                                                 @RequestParam(value = "auditRemark", required = false) String auditRemark) {
        correctionService.auditCorrection(id, status, auditRemark, getLoginUserId());
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出补卡申请 Excel")
    @PreAuthorize("@ss.hasPermission('biz:correction:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCorrectionExcel(@Valid AttendanceCorrectionPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<AttendanceCorrectionRespVO> list = BeanUtils.toBean(
                correctionService.getCorrectionPage(pageReqVO).getList(), AttendanceCorrectionRespVO.class);
        ExcelUtils.write(response, "补卡申请.xls", "数据", AttendanceCorrectionRespVO.class, list);
    }
}
