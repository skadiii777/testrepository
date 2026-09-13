package com.enterprise.module.biz.controller.admin.fms;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;
import com.enterprise.module.biz.service.fms.FmsVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 记账凭证")
@RestController
@RequestMapping("/biz/fms/voucher")
@Validated
public class FmsVoucherController {

    @Resource
    private FmsVoucherService voucherService;

    @PostMapping("/create")
    @Operation(summary = "创建凭证")
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:create')")
    public CommonResult<Long> createVoucher(@Valid @RequestBody FmsVoucherSaveReqVO reqVO) {
        return success(voucherService.createVoucher(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改凭证（仅草稿）")
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:update')")
    public CommonResult<Boolean> updateVoucher(@Valid @RequestBody FmsVoucherSaveReqVO reqVO) {
        voucherService.updateVoucher(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除凭证（仅草稿）")
    @Parameter(name = "id", description = "凭证编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:delete')")
    public CommonResult<Boolean> deleteVoucher(@RequestParam("id") Long id) {
        voucherService.deleteVoucher(id);
        return success(true);
    }

    @PutMapping("/post")
    @Operation(summary = "凭证记账（草稿→已记账）")
    @Parameter(name = "id", description = "凭证编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:post')")
    public CommonResult<Boolean> postVoucher(@RequestParam("id") Long id) {
        voucherService.postVoucher(id);
        return success(true);
    }

    @PutMapping("/unpost")
    @Operation(summary = "取消记账（已记账→草稿）")
    @Parameter(name = "id", description = "凭证编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:post')")
    public CommonResult<Boolean> unpostVoucher(@RequestParam("id") Long id) {
        voucherService.unpostVoucher(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得凭证（含分录明细）")
    @Parameter(name = "id", description = "凭证编号", required = true)
    public CommonResult<FmsVoucherRespVO> getVoucher(@RequestParam("id") Long id) {
        return success(voucherService.getVoucher(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得凭证分页")
    @PreAuthorize("@ss.hasPermission('biz:fms:voucher:query')")
    public CommonResult<PageResult<FmsVoucherRespVO>> getVoucherPage(@Valid FmsVoucherPageReqVO pageReqVO) {
        return success(voucherService.getVoucherPage(pageReqVO));
    }
}
