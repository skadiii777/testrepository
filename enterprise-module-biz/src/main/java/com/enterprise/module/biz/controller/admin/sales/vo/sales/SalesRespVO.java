package com.enterprise.module.biz.controller.admin.sales.vo.sales;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 销售单 Response VO")
@Data
public class SalesRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "销售单号")
    private String salesCode;
    @Schema(description = "客户")
    private String customerName;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "销售数量")
    private Long quantity;
    @Schema(description = "销售单价")
    private BigDecimal price;
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    @Schema(description = "销售日期")
    private String salesDate;
    @Schema(description = "出库状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}