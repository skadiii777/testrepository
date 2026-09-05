package com.enterprise.module.biz.controller.admin.stockmove;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove.*;
import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;
import com.enterprise.module.biz.service.stockmove.StockMoveService;
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

@Tag(name = "管理后台 - 库存流水")
@RestController
@RequestMapping("/biz/stockmove")
@Validated
public class StockMoveController {

    @Resource
    private StockMoveService stockmoveService;

    @GetMapping("/get")
    @Operation(summary = "获得库存流水")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('biz:stockmove:query')")
    public CommonResult<StockMoveRespVO> getStockMove(@RequestParam("id") Long id) {
        StockMoveDO stockmove = stockmoveService.getStockMove(id);
        return success(BeanUtils.toBean(stockmove, StockMoveRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存流水分页")
    @PreAuthorize("@ss.hasPermission('biz:stockmove:query')")
    public CommonResult<PageResult<StockMoveRespVO>> getStockMovePage(@Valid StockMovePageReqVO pageReqVO) {
        PageResult<StockMoveDO> pageResult = stockmoveService.getStockMovePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StockMoveRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存流水 Excel")
    @PreAuthorize("@ss.hasPermission('biz:stockmove:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockMoveExcel(@Valid StockMovePageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<StockMoveRespVO> list = BeanUtils.toBean(stockmoveService.getStockMovePage(pageReqVO).getList(), StockMoveRespVO.class);
        ExcelUtils.write(response, "库存流水.xls", "数据", StockMoveRespVO.class, list);
    }
}