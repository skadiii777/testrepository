package com.enterprise.module.biz.controller.admin.purchase;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.*;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.enterprise.module.biz.service.purchase.PurchaseService;
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

@Tag(name = "管理后台 - 采购单")
@RestController
@RequestMapping("/biz/purchase")
@Validated
public class PurchaseController {

    @Resource
    private PurchaseService purchaseService;

    @PostMapping("/create")
    @Operation(summary = "创建采购单")
    @PreAuthorize("@ss.hasPermission('biz:purchase:create')")
    public CommonResult<Long> createPurchase(@Valid @RequestBody PurchaseSaveReqVO createReqVO) {
        return success(purchaseService.createPurchase(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新采购单")
    @PreAuthorize("@ss.hasPermission('biz:purchase:update')")
    public CommonResult<Boolean> updatePurchase(@Valid @RequestBody PurchaseSaveReqVO updateReqVO) {
        purchaseService.updatePurchase(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除采购单")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:purchase:delete')")
    public CommonResult<Boolean> deletePurchase(@RequestParam("id") Long id) {
        purchaseService.deletePurchase(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得采购单")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:purchase:query')")
    public CommonResult<PurchaseRespVO> getPurchase(@RequestParam("id") Long id) {
        PurchaseDO purchase = purchaseService.getPurchase(id);
        return success(BeanUtils.toBean(purchase, PurchaseRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得采购单分页")
    @PreAuthorize("@ss.hasPermission('biz:purchase:query')")
    public CommonResult<PageResult<PurchaseRespVO>> getPurchasePage(@Valid PurchasePageReqVO pageReqVO) {
        PageResult<PurchaseDO> pageResult = purchaseService.getPurchasePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, PurchaseRespVO.class));
    }

    @PostMapping("/transition")
    @Operation(summary = "采购单状态流转（confirm=确认 / void=作废）")
    @PreAuthorize("@ss.hasPermission('biz:purchase:confirm')")
    public CommonResult<Boolean> transitionPurchase(@RequestParam("id") Long id,
                                                    @RequestParam("action") String action) {
        if ("void".equals(action)) {
            purchaseService.transitionPurchase(id, "void");
        } else {
            purchaseService.transitionPurchase(id, "confirm");
        }
        return success(true);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成采购单（自动入库）")
    @PreAuthorize("@ss.hasPermission('biz:purchase:complete')")
    public CommonResult<Boolean> completePurchase(@RequestParam("id") Long id) {
        purchaseService.completePurchase(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出采购单 Excel")
    @PreAuthorize("@ss.hasPermission('biz:purchase:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPurchaseExcel(@Valid PurchasePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<PurchaseRespVO> list = BeanUtils.toBean(purchaseService.getPurchasePage(pageReqVO).getList(), PurchaseRespVO.class);
        ExcelUtils.write(response, "采购单.xls", "数据", PurchaseRespVO.class, list);
    }
}