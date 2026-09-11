package com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 库存盘点创建 Request VO")
@Data
public class StockCheckCreateReqVO {

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;
    private Long warehouseId;
    private String productName;

    @Schema(description = "仓库（默认默认仓库）")
    private String warehouse;

    @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "实盘数量不能为空")
    @Min(value = 0, message = "实盘数量不能为负数")
    private Long actualQuantity;

    @Schema(description = "盘点日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "盘点日期不能为空")
    private String checkDate;

    @Schema(description = "备注")
    private String remark;

}
