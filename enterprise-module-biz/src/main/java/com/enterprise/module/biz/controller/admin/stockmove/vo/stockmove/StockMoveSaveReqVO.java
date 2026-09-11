package com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 库存流水新增/修改 Request VO")
@Data
public class StockMoveSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;
    private Long warehouseId;
    private String productName;
    @Schema(description = "仓库")
    
    private String warehouse;
    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="类型不能为空")
    private String moveType;
    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="数量不能为空")
    private BigDecimal quantity;
    @Schema(description = "结余", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="结余不能为空")
    private BigDecimal balanceAfter;
    @Schema(description = "来源类型")
    
    private String sourceType;
    @Schema(description = "来源单号")
    
    private String sourceCode;
}