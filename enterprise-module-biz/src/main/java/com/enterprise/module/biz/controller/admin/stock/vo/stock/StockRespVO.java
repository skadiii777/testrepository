package com.enterprise.module.biz.controller.admin.stock.vo.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 库存 Response VO")
@Data
public class StockRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String productName;
    @Schema(description = "仓库")
    private String warehouse;
    @Schema(description = "库存数量")
    private Long quantity;
    @Schema(description = "预警下限")
    private Long minQuantity;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}