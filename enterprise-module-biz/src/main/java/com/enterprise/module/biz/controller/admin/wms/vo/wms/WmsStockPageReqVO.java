package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 库位库存分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsStockPageReqVO extends PageParam {
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "库位ID")
    private Long locationId;
    @Schema(description = "产品名称")
    private String productName;
}
