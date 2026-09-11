package com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 库存盘点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StockCheckPageReqVO extends PageParam {

    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String productName;

    @Schema(description = "状态（0=待确认 1=已确认）")
    private String status;

    @Schema(description = "盘点日期范围")
    private String[] checkDateRange;

}
