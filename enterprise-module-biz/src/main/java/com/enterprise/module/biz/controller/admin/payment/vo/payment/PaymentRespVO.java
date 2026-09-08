package com.enterprise.module.biz.controller.admin.payment.vo.payment;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 收付款流水 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PaymentRespVO {

    @Schema(description = "主键")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "收付单号")
    @ExcelProperty("收付单号")
    private String paymentNo;

    @Schema(description = "收付类型（1=收款 2=付款）")
    @ExcelProperty("收付类型")
    private String paymentType;

    @Schema(description = "关联单据类型（1=销售单 2=采购单）")
    @ExcelProperty("单据类型")
    private String bizType;

    @Schema(description = "关联单据 id")
    private Long orderId;

    @Schema(description = "关联单据编号")
    @ExcelProperty("关联单据")
    private String orderCode;

    @Schema(description = "对方名称（客户/供应商）")
    @ExcelProperty("对方名称")
    private String partyName;

    @Schema(description = "金额")
    @ExcelProperty("金额")
    private BigDecimal amount;

    @Schema(description = "收付方式")
    @ExcelProperty("收付方式")
    private String paymentMethod;

    @Schema(description = "收付日期")
    @ExcelProperty("收付日期")
    private String paymentDate;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

}
