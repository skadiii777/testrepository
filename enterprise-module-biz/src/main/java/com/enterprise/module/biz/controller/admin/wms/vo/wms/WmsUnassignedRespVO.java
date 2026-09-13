package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS 未分配库存 Response VO（仓库库存 - 库位分配合计）")
@Data
public class WmsUnassignedRespVO {
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "仓库名称")
    private String warehouseName;
    @Schema(description = "产品ID")
    private Long productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "仓库库存总量")
    private Long mainQuantity;
    @Schema(description = "已分配到库位合计")
    private Long allocated;
    @Schema(description = "未分配量（可上架）")
    private Long unassigned;
}
