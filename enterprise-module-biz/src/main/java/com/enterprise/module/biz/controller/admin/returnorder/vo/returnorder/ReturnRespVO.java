package com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 退货单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ReturnRespVO {

    @Schema(description = "主键")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "退货单号")
    @ExcelProperty("退货单号")
    private String returnNo;

    @Schema(description = "退货类型（1=销售退货 2=采购退货）")
    @ExcelProperty("退货类型")
    private String returnType;

    @Schema(description = "关联单据 id")
    private Long orderId;

    @Schema(description = "关联单据编号")
    @ExcelProperty("关联单据")
    private String orderCode;

    @Schema(description = "对方名称（客户/供应商）")
    @ExcelProperty("对方名称")
    private String partyName;

    @Schema(description = "产品名称")
    @ExcelProperty("产品名称")
    private Long productId;
    private Long warehouseId;
    private String productName;

    @Schema(description = "仓库")
    @ExcelProperty("仓库")
    private String warehouse;

    @Schema(description = "退货数量")
    @ExcelProperty("退货数量")
    private Long quantity;

    @Schema(description = "退货单价")
    @ExcelProperty("退货单价")
    private BigDecimal price;

    @Schema(description = "总金额")
    @ExcelProperty("总金额")
    private BigDecimal totalAmount;

    @Schema(description = "退货日期")
    @ExcelProperty("退货日期")
    private String returnDate;

    @Schema(description = "退货原因")
    @ExcelProperty("退货原因")
    private String reason;

    @Schema(description = "状态（0=待退货 1=已退货 3=已作废）")
    @ExcelProperty("状态")
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

}
