package com.enterprise.module.biz.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 商机转合同 Request VO")
@Data
public class BusinessContractConvertReqVO {

    @Schema(description = "产品名称（商机未携带产品，需指定）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "产品名称不能为空")
    private String productName;

    @Schema(description = "合同开始日期（留空=今天）")
    private String startDate;

    @Schema(description = "合同结束日期（留空=一年后）")
    private String endDate;

}
