package com.enterprise.module.biz.controller.admin.quota;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.quota.vo.quota.*;
import com.enterprise.module.biz.dal.dataobject.quota.LeaveQuotaDO;
import com.enterprise.module.biz.service.quota.LeaveQuotaService;
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

@Tag(name = "管理后台 - 假期余额")
@RestController
@RequestMapping("/biz/quota")
@Validated
public class LeaveQuotaController {

    @Resource
    private LeaveQuotaService quotaService;

    @PostMapping("/create")
    @Operation(summary = "创建假期余额")
    @PreAuthorize("@ss.hasPermission('biz:quota:create')")
    public CommonResult<Long> createLeaveQuota(@Valid @RequestBody LeaveQuotaSaveReqVO createReqVO) {
        return success(quotaService.createLeaveQuota(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新假期余额")
    @PreAuthorize("@ss.hasPermission('biz:quota:update')")
    public CommonResult<Boolean> updateLeaveQuota(@Valid @RequestBody LeaveQuotaSaveReqVO updateReqVO) {
        quotaService.updateLeaveQuota(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除假期余额")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:quota:delete')")
    public CommonResult<Boolean> deleteLeaveQuota(@RequestParam("id") Long id) {
        quotaService.deleteLeaveQuota(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得假期余额")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:quota:query')")
    public CommonResult<LeaveQuotaRespVO> getLeaveQuota(@RequestParam("id") Long id) {
        LeaveQuotaDO quota = quotaService.getLeaveQuota(id);
        return success(BeanUtils.toBean(quota, LeaveQuotaRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得假期余额分页")
    @PreAuthorize("@ss.hasPermission('biz:quota:query')")
    public CommonResult<PageResult<LeaveQuotaRespVO>> getLeaveQuotaPage(@Valid LeaveQuotaPageReqVO pageReqVO) {
        PageResult<LeaveQuotaDO> pageResult = quotaService.getLeaveQuotaPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, LeaveQuotaRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出假期余额 Excel")
    @PreAuthorize("@ss.hasPermission('biz:quota:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportLeaveQuotaExcel(@Valid LeaveQuotaPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<LeaveQuotaRespVO> list = BeanUtils.toBean(quotaService.getLeaveQuotaPage(pageReqVO).getList(), LeaveQuotaRespVO.class);
        ExcelUtils.write(response, "假期余额.xls", "数据", LeaveQuotaRespVO.class, list);
    }
}