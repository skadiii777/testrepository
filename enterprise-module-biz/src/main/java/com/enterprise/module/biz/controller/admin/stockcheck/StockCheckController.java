package com.enterprise.module.biz.controller.admin.stockcheck;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckCreateReqVO;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckPageReqVO;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckRespVO;
import com.enterprise.module.biz.dal.dataobject.stockcheck.StockCheckDO;
import com.enterprise.module.biz.service.stockcheck.StockCheckService;
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

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.DELETE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.enterprise.framework.excel.core.util.ExcelUtils.write;

@Tag(name = "管理后台 - 库存盘点")
@RestController
@RequestMapping("/biz/stockcheck")
@Validated
public class StockCheckController {

    @Resource
    private StockCheckService stockCheckService;

    @PostMapping("/create")
    @Operation(summary = "创建盘点单")
    @PreAuthorize("@ss.hasPermission('biz:stockcheck:create')")
    public CommonResult<Long> createStockCheck(@Valid @RequestBody StockCheckCreateReqVO createReqVO) {
        return success(stockCheckService.createStockCheck(createReqVO));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认盘点（按实盘调整库存并写流水）")
    @Parameter(name = "id", description = "盘点单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:stockcheck:confirm')")
    public CommonResult<Boolean> confirmStockCheck(@RequestParam("id") Long id) {
        stockCheckService.confirmStockCheck(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除盘点单（仅待确认）")
    @Parameter(name = "id", description = "盘点单编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:stockcheck:delete')")
    public CommonResult<Boolean> deleteStockCheck(@RequestParam("id") Long id) {
        stockCheckService.deleteStockCheck(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得盘点单分页")
    @PreAuthorize("@ss.hasPermission('biz:stockcheck:query')")
    public CommonResult<PageResult<StockCheckRespVO>> getStockCheckPage(@Valid StockCheckPageReqVO pageReqVO) {
        PageResult<StockCheckDO> pageResult = stockCheckService.getStockCheckPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StockCheckRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出盘点单 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:stockcheck:query')")
    public void exportStockCheckExcel(@Valid StockCheckPageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<StockCheckRespVO> list = BeanUtils.toBean(
                stockCheckService.getStockCheckPage(pageReqVO).getList(), StockCheckRespVO.class);
        write(response, "库存盘点.xls", "数据", StockCheckRespVO.class, list);
    }

}
