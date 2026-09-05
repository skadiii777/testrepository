package com.enterprise.module.biz.controller.admin.stock.vo.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 库存新增/修改 Request VO")
@Data
public class StockSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="产品名称不能为空")
    private String productName;
    @Schema(description = "仓库")
    
    private String warehouse;
    @Schema(description = "库存数量")
    
    private Long quantity;
    @Schema(description = "预警下限")
    
    private Long minQuantity;
}