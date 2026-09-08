package com.enterprise.module.biz.controller.admin.payment.vo.payment;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 收付款流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentPageReqVO extends PageParam {

    @Schema(description = "收付类型（1=收款 2=付款）")
    private String paymentType;

    @Schema(description = "关联单据类型（1=销售单 2=采购单）")
    private String bizType;

    @Schema(description = "关联单据编号")
    private String orderCode;

    @Schema(description = "对方名称（客户/供应商）")
    private String partyName;

    @Schema(description = "收付日期范围")
    private String[] paymentDateRange;

}
