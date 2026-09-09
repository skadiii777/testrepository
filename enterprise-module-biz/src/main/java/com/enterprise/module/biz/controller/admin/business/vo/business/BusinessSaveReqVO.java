package com.enterprise.module.biz.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 商机新增/修改 Request VO")
@Data
public class BusinessSaveReqVO {

    @Schema(description = "主键（更新时必填）")
    private Long id;

    @Schema(description = "商机名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商机名称不能为空")
    private String name;

    @Schema(description = "关联客户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "关联客户不能为空")
    private String customerName;

    @Schema(description = "阶段（biz_business_stage：1-6，5赢单/6输单为终局）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商机阶段不能为空")
    private String stage;

    @Schema(description = "预期金额")
    private BigDecimal amount;

    @Schema(description = "预计成交日期")
    private String expectedDate;

    @Schema(description = "备注")
    private String remark;

}
