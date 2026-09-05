package com.enterprise.module.biz.controller.admin.purchase.vo.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 采购单新增/修改 Request VO")
@Data
public class PurchaseSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "采购单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="采购单号不能为空")
    private String purchaseCode;
    @Schema(description = "供应商")
    
    private String supplierName;
    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="产品名称不能为空")
    private String productName;
    @Schema(description = "采购数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="采购数量不能为空")
    private Long quantity;
    @Schema(description = "采购单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="采购单价不能为空")
    private BigDecimal price;
    @Schema(description = "总金额")
    
    private BigDecimal totalAmount;
    @Schema(description = "采购日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="采购日期不能为空")
    private String purchaseDate;
    @Schema(description = "状态（0待处理 1已完成）")
    private String status;
}