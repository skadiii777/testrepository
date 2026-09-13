package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 记账凭证分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsVoucherPageReqVO extends PageParam {
    @Schema(description = "凭证号")
    private String voucherNo;
    @Schema(description = "状态（0草稿 1已记账）")
    private Integer status;
    @Schema(description = "来源类型（payment=收付款 return=退货 manual=手工）", example = "payment")
    private String sourceType;
    @Schema(description = "凭证日期范围")
    private String[] voucherDateRange;
}
