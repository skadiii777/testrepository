package com.enterprise.module.biz.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 产品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="产品编号不能为空")
    private String productCode;
    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="产品名称不能为空")
    private String productName;
    @Schema(description = "产品分类")
    
    private String category;
    @Schema(description = "单位")
    
    private String unit;
    @Schema(description = "销售单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="销售单价不能为空")
    private BigDecimal price;
    @Schema(description = "成本价")
    
    private BigDecimal cost;
    @Schema(description = "状态")
    
    private String status;
}