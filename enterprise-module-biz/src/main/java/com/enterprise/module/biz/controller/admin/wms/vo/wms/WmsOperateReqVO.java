package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS 库位动作（上架/下架/移库）Request VO")
@Data
public class WmsOperateReqVO {
    @Schema(description = "产品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "产品不能为空")
    private Long productId;
    @Schema(description = "仓库ID（上架时必填，其余从库位带出）")
    private Long warehouseId;
    @Schema(description = "目标库位ID（上架/移库的目标）")
    private Long locationId;
    @Schema(description = "源库位ID（下架/移库的来源）")
    private Long fromLocationId;
    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Long quantity;
    @Schema(description = "备注")
    private String remark;
}
