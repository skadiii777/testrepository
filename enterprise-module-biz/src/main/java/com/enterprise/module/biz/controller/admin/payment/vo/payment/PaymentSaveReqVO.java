package com.enterprise.module.biz.controller.admin.payment.vo.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 收付款流水新增 Request VO")
@Data
public class PaymentSaveReqVO {

    @Schema(description = "收付类型（1=收款 2=付款）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收付类型不能为空")
    private String paymentType;

    @Schema(description = "关联单据类型（1=销售单 2=采购单）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "关联单据类型不能为空")
    private String bizType;

    @Schema(description = "关联单据 id（与合同二选一：登记单据收付款必填；纯合同回款可空）")
    private Long orderId;

    @Schema(description = "金额", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    @Schema(description = "收付方式")
    private String paymentMethod;

    @Schema(description = "关联合同 id（收款时可选拉通合同回款进度）")
    private Long contractId;

    @Schema(description = "收付日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收付日期不能为空")
    private String paymentDate;

    @Schema(description = "备注")
    private String remark;

}
