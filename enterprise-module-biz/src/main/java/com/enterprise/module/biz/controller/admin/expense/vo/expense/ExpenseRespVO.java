package com.enterprise.module.biz.controller.admin.expense.vo.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 费用报销 Response VO")
@Data
public class ExpenseRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "报销人")
    private String empName;
    @Schema(description = "费用类别")
    private String category;
    @Schema(description = "金额")
    private BigDecimal amount;
    @Schema(description = "费用发生日期")
    private String expenseDate;
    @Schema(description = "费用说明")
    private String reason;
    @Schema(description = "审批状态")
    private String status;
    @Schema(description = "审批意见")
    private String auditRemark;
    @Schema(description = "审批人")
    private String auditBy;
    @Schema(description = "审批时间")
    private String auditTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}