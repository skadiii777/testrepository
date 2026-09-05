package com.enterprise.module.biz.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 产品 Response VO")
@Data
public class ProductRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品编号")
    private String productCode;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "产品分类")
    private String category;
    @Schema(description = "单位")
    private String unit;
    @Schema(description = "销售单价")
    private BigDecimal price;
    @Schema(description = "成本价")
    private BigDecimal cost;
    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}