package com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 退货单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReturnPageReqVO extends PageParam {

    @Schema(description = "退货类型（1=销售退货 2=采购退货）")
    private String returnType;

    @Schema(description = "状态（0=待退货 1=已退货 3=已作废）")
    private String status;

    @Schema(description = "关联单据编号")
    private String orderCode;

    @Schema(description = "对方名称（客户/供应商）")
    private String partyName;

    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String productName;

    @Schema(description = "退货日期范围")
    private String[] returnDateRange;

}
