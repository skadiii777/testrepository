package com.enterprise.module.biz.controller.admin.expense.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 费用报销新增/修改 Request VO")
@Data
public class ExpenseSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "报销人")
    
    private String empName;
    @Schema(description = "费用类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="费用类别不能为空")
    private String category;
    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="金额不能为空")
    private BigDecimal amount;
    @Schema(description = "费用发生日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="费用发生日期不能为空")
    private String expenseDate;
    @Schema(description = "费用说明")
    
    private String reason;
    @Schema(description = "审批状态")
    
    private String status;
}