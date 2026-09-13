package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - WMS 库位流水 Response VO")
@Data
public class WmsMoveRespVO {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "动作类型（putaway/remove/move）")
    private String moveType;
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "产品ID")
    private Long productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "数量")
    private Long quantity;
    @Schema(description = "源库位编码")
    private String fromLocationCode;
    @Schema(description = "目标库位编码")
    private String toLocationCode;
    @Schema(description = "操作人")
    private String operatorName;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
