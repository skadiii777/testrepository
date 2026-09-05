package com.enterprise.module.biz.controller.admin.sales;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.*;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.enterprise.module.biz.service.sales.SalesService;
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

@Tag(name = "管理后台 - 销售单")
@RestController
@RequestMapping("/biz/sales")
@Validated
public class SalesController {

    @Resource
    private SalesService salesService;

    @PostMapping("/create")
    @Operation(summary = "创建销售单")
    @PreAuthorize("@ss.hasPermission('biz:sales:create')")
    public CommonResult<Long> createSales(@Valid @RequestBody SalesSaveReqVO createReqVO) {
        return success(salesService.createSales(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售单")
    @PreAuthorize("@ss.hasPermission('biz:sales:update')")
    public CommonResult<Boolean> updateSales(@Valid @RequestBody SalesSaveReqVO updateReqVO) {
        salesService.updateSales(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售单")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:sales:delete')")
    public CommonResult<Boolean> deleteSales(@RequestParam("id") Long id) {
        salesService.deleteSales(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得销售单")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:sales:query')")
    public CommonResult<SalesRespVO> getSales(@RequestParam("id") Long id) {
        SalesDO sales = salesService.getSales(id);
        return success(BeanUtils.toBean(sales, SalesRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售单分页")
    @PreAuthorize("@ss.hasPermission('biz:sales:query')")
    public CommonResult<PageResult<SalesRespVO>> getSalesPage(@Valid SalesPageReqVO pageReqVO) {
        PageResult<SalesDO> pageResult = salesService.getSalesPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SalesRespVO.class));
    }

    @PostMapping("/transition")
    @Operation(summary = "销售单状态流转（confirm=确认 / void=作废）")
    @PreAuthorize("@ss.hasPermission('biz:sales:confirm')")
    public CommonResult<Boolean> transitionSales(@RequestParam("id") Long id,
                                                 @RequestParam("action") String action) {
        if ("void".equals(action)) {
            salesService.transitionSales(id, "void");
        } else {
            salesService.transitionSales(id, "confirm");
        }
        return success(true);
    }

    @PostMapping("/complete")
    @Operation(summary = "完成销售单（自动出库，库存不足拒绝）")
    @PreAuthorize("@ss.hasPermission('biz:sales:complete')")
    public CommonResult<Boolean> completeSales(@RequestParam("id") Long id) {
        salesService.completeSales(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售单 Excel")
    @PreAuthorize("@ss.hasPermission('biz:sales:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSalesExcel(@Valid SalesPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<SalesRespVO> list = BeanUtils.toBean(salesService.getSalesPage(pageReqVO).getList(), SalesRespVO.class);
        ExcelUtils.write(response, "销售单.xls", "数据", SalesRespVO.class, list);
    }
}