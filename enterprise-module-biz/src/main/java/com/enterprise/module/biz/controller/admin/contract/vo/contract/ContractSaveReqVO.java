package com.enterprise.module.biz.controller.admin.contract.vo.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 合同新增/修改 Request VO")
@Data
public class ContractSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="合同编号不能为空")
    private String contractCode;
    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="客户名称不能为空")
    private String customerName;
    @Schema(description = "产品名称")
    
    private String productName;
    @Schema(description = "合同金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="合同金额不能为空")
    private BigDecimal amount;
    @Schema(description = "签订日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="签订日期不能为空")
    private String signDate;
    @Schema(description = "开始日期")
    
    private String startDate;
    @Schema(description = "结束日期")
    
    private String endDate;
    @Schema(description = "负责人")
    
    private String owner;
    @Schema(description = "合同状态")
    
    private String status;
}