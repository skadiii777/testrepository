package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 库位流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsMovePageReqVO extends PageParam {
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "动作类型（putaway/remove/move）")
    private String moveType;
}
