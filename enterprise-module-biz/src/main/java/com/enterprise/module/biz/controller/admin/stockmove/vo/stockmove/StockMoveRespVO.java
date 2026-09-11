package com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 库存流水 Response VO")
@Data
public class StockMoveRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String productName;
    @Schema(description = "仓库")
    private String warehouse;
    @Schema(description = "类型")
    private String moveType;
    @Schema(description = "数量")
    private BigDecimal quantity;
    @Schema(description = "结余")
    private BigDecimal balanceAfter;
    @Schema(description = "来源类型")
    private String sourceType;
    @Schema(description = "来源单号")
    private String sourceCode;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}