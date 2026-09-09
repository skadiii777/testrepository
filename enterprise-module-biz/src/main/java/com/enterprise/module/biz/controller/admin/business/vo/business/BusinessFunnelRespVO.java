package com.enterprise.module.biz.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 商机漏斗统计 Response VO")
@Data
public class BusinessFunnelRespVO {

    @Schema(description = "阶段（biz_business_stage：1-6）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stage;

    @Schema(description = "阶段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stageName;

    @Schema(description = "商机数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long count;

    @Schema(description = "预期金额合计", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

}
