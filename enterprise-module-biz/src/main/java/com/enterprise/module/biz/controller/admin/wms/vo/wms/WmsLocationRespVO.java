package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 库位 Response VO")
@Data
public class WmsLocationRespVO {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "仓库名称")
    private String warehouseName;
    @Schema(description = "库位编码")
    private String code;
    @Schema(description = "库位名称")
    private String name;
    @Schema(description = "库位类型")
    private Integer type;
    @Schema(description = "状态（0启用 1停用）")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
