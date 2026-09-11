package com.enterprise.module.biz.controller.admin.purchase.vo.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 采购单 Response VO")
@Data
public class PurchaseRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "采购单号")
    private String purchaseCode;
    @Schema(description = "供应商")
    private String supplierName;
    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String warehouse;
    private String productName;
    @Schema(description = "采购数量")
    private Long quantity;
    @Schema(description = "采购单价")
    private BigDecimal price;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "采购日期")
    private String purchaseDate;
    @Schema(description = "入库状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}