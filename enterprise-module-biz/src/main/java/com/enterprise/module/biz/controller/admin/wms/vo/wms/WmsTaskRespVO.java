package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - WMS 作业任务 Response VO（上架/拣货统一视图）")
@Data
public class WmsTaskRespVO {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "任务类型（putaway上架 pick拣货）")
    private String type;
    @Schema(description = "来源单号（采购单号/销售单号）")
    private String sourceCode;
    @Schema(description = "产品ID")
    private Long productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "仓库名称")
    private String warehouseName;
    @Schema(description = "任务数量")
    private Long quantity;
    @Schema(description = "已完成数量")
    private Long doneQuantity;
    @Schema(description = "状态（0进行中 1已完成）")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
