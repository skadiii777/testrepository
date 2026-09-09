package com.enterprise.module.biz.controller.admin.clue.vo.clue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 线索转商机 Request VO")
@Data
public class ClueConvertReqVO {

    @Schema(description = "商机名称（默认带线索名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商机名称不能为空")
    private String businessName;

    @Schema(description = "初始阶段（biz_business_stage，留空=1初步接触）")
    private String stage;

    @Schema(description = "预期金额")
    private BigDecimal amount;

    @Schema(description = "预计成交日期")
    private String expectedDate;

}
