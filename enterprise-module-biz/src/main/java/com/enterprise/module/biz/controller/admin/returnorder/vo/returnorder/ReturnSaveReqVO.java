package com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 退货单新增/修改 Request VO")
@Data
public class ReturnSaveReqVO {

    @Schema(description = "主键（更新时必填）")
    private Long id;

    @Schema(description = "退货类型（1=销售退货 2=采购退货）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "退货类型不能为空")
    private String returnType;

    @Schema(description = "关联单据 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联单据不能为空")
    private Long orderId;

    @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退货数量不能为空")
    private Long quantity;

    @Schema(description = "退货单价（留空取原单单价）")
    private BigDecimal price;

    @Schema(description = "仓库（留空取默认仓库）")
    private String warehouse;

    @Schema(description = "退货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "退货日期不能为空")
    private String returnDate;

    @Schema(description = "退货原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "退货原因不能为空")
    private String reason;

    @Schema(description = "备注")
    private String remark;

}
