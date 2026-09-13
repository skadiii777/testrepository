package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - WMS 库位新增/修改 Request VO")
@Data
public class WmsLocationSaveReqVO {
    @Schema(description = "主键（更新时必填）")
    private Long id;
    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;
    @Schema(description = "库位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "库位编码不能为空")
    private String code;
    @Schema(description = "库位名称")
    private String name;
    @Schema(description = "库位类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库位类型不能为空")
    private Integer type;
    @Schema(description = "状态（0启用 1停用）")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
}
