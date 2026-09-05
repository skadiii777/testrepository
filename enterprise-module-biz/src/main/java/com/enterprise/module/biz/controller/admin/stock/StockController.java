package com.enterprise.module.biz.controller.admin.stock;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.stock.vo.stock.*;
import com.enterprise.module.biz.dal.dataobject.stock.StockDO;
import com.enterprise.module.biz.service.stock.StockService;
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

@Tag(name = "管理后台 - 库存")
@RestController
@RequestMapping("/biz/stock")
@Validated
public class StockController {

    @Resource
    private StockService stockService;

    @PostMapping("/create")
    @Operation(summary = "创建库存")
    @PreAuthorize("@ss.hasPermission('biz:stock:create')")
    public CommonResult<Long> createStock(@Valid @RequestBody StockSaveReqVO createReqVO) {
        return success(stockService.createStock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存")
    @PreAuthorize("@ss.hasPermission('biz:stock:update')")
    public CommonResult<Boolean> updateStock(@Valid @RequestBody StockSaveReqVO updateReqVO) {
        stockService.updateStock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:stock:delete')")
    public CommonResult<Boolean> deleteStock(@RequestParam("id") Long id) {
        stockService.deleteStock(id);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得库存")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:stock:query')")
    public CommonResult<StockRespVO> getStock(@RequestParam("id") Long id) {
        StockDO stock = stockService.getStock(id);
        return success(BeanUtils.toBean(stock, StockRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存分页")
    @PreAuthorize("@ss.hasPermission('biz:stock:query')")
    public CommonResult<PageResult<StockRespVO>> getStockPage(@Valid StockPageReqVO pageReqVO) {
        PageResult<StockDO> pageResult = stockService.getStockPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StockRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存 Excel")
    @PreAuthorize("@ss.hasPermission('biz:stock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockExcel(@Valid StockPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<StockRespVO> list = BeanUtils.toBean(stockService.getStockPage(pageReqVO).getList(), StockRespVO.class);
        ExcelUtils.write(response, "库存.xls", "数据", StockRespVO.class, list);
    }
}