package com.enterprise.module.biz.controller.admin.wms;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;
import com.enterprise.module.biz.dal.dataobject.wms.WmsLocationStockDO;
import com.enterprise.module.biz.service.wms.WmsStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - WMS 库位库存")
@RestController
@RequestMapping("/biz/wms/stock")
@Validated
public class WmsStockController {

    @Resource
    private WmsStockService stockService;
    @Resource
    private com.enterprise.module.biz.service.wms.WmsTaskService taskService;

    @GetMapping("/page")
    @Operation(summary = "获得库位库存分页")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:query')")
    public CommonResult<PageResult<WmsLocationStockDO>> getStockPage(@Valid WmsStockPageReqVO pageReqVO) {
        return success(stockService.getStockPage(pageReqVO));
    }

    @GetMapping("/unassigned")
    @Operation(summary = "未分配库存列表（仓库库存 - 库位合计，可按仓库过滤）")
    @Parameter(name = "warehouseId", description = "仓库编号（可选）")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:query')")
    public CommonResult<List<WmsUnassignedRespVO>> getUnassigned(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        return success(stockService.getUnassigned(warehouseId));
    }

    @PutMapping("/putaway")
    @Operation(summary = "上架（未分配 → 库位）")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:operate')")
    public CommonResult<Boolean> putaway(@Valid @RequestBody WmsOperateReqVO reqVO) {
        stockService.putaway(reqVO);
        return success(true);
    }

    @PutMapping("/remove")
    @Operation(summary = "下架（库位 → 未分配）")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:operate')")
    public CommonResult<Boolean> remove(@Valid @RequestBody WmsOperateReqVO reqVO) {
        stockService.remove(reqVO);
        return success(true);
    }

    @PutMapping("/move")
    @Operation(summary = "移库（同仓库库位间移动）")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:operate')")
    public CommonResult<Boolean> move(@Valid @RequestBody WmsOperateReqVO reqVO) {
        stockService.move(reqVO);
        return success(true);
    }

    @GetMapping("/move-page")
    @Operation(summary = "获得库位流水分页")
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:query')")
    public CommonResult<PageResult<WmsMoveRespVO>> getMovePage(@Valid WmsMovePageReqVO pageReqVO) {
        return success(stockService.getMovePage(pageReqVO));
    }

    @GetMapping("/task-page")
    @Operation(summary = "获得作业任务分页（type=putaway 上架 / pick 拣货）")
    @Parameter(name = "type", description = "任务类型", required = true)
    @PreAuthorize("@ss.hasPermission('biz:wms:stock:query')")
    public CommonResult<PageResult<WmsTaskRespVO>> getTaskPage(
            @RequestParam("type") String type, @Valid WmsTaskPageReqVO pageReqVO) {
        return success(taskService.getTaskPage(type, pageReqVO));
    }
}
