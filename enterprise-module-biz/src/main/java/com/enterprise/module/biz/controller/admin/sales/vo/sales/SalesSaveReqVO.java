package com.enterprise.module.biz.controller.admin.sales.vo.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 销售单新增/修改 Request VO")
@Data
public class SalesSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "销售单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="销售单号不能为空")
    private String salesCode;
    @Schema(description = "客户")
    
    private String customerName;
    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="产品名称不能为空")
    private String productName;
    @Schema(description = "销售数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="销售数量不能为空")
    private Long quantity;
    @Schema(description = "销售单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="销售单价不能为空")
    private BigDecimal price;
    @Schema(description = "总金额")
    
    private BigDecimal totalAmount;
    @Schema(description = "销售日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="销售日期不能为空")
    private String salesDate;
    @Schema(description = "状态（0待处理 1已完成）")
    private String status;
}