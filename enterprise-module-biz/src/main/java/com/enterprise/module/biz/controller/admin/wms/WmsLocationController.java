package com.enterprise.module.biz.controller.admin.wms;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.wms.vo.wms.*;
import com.enterprise.module.biz.service.wms.WmsLocationService;
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

@Tag(name = "管理后台 - WMS 库位")
@RestController
@RequestMapping("/biz/wms/location")
@Validated
public class WmsLocationController {

    @Resource
    private WmsLocationService locationService;

    @PostMapping("/create")
    @Operation(summary = "创建库位")
    @PreAuthorize("@ss.hasPermission('biz:wms:create')")
    public CommonResult<Long> createLocation(@Valid @RequestBody WmsLocationSaveReqVO reqVO) {
        return success(locationService.createLocation(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改库位")
    @PreAuthorize("@ss.hasPermission('biz:wms:update')")
    public CommonResult<Boolean> updateLocation(@Valid @RequestBody WmsLocationSaveReqVO reqVO) {
        locationService.updateLocation(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库位（有库存时拒绝）")
    @Parameter(name = "id", description = "库位编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:wms:delete')")
    public CommonResult<Boolean> deleteLocation(@RequestParam("id") Long id) {
        locationService.deleteLocation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库位")
    @Parameter(name = "id", description = "库位编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:wms:query')")
    public CommonResult<WmsLocationRespVO> getLocation(@RequestParam("id") Long id) {
        return success(locationService.getLocation(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库位分页")
    @PreAuthorize("@ss.hasPermission('biz:wms:query')")
    public CommonResult<PageResult<WmsLocationRespVO>> getLocationPage(@Valid WmsLocationPageReqVO pageReqVO) {
        return success(locationService.getLocationPage(pageReqVO));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得启用库位精简列表（库存动作下拉用，登录即可）")
    @Parameter(name = "warehouseId", description = "仓库编号（可选过滤）")
    public CommonResult<List<WmsLocationRespVO>> getSimpleLocationList(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId) {
        return success(locationService.getSimpleLocationList(warehouseId));
    }
}
