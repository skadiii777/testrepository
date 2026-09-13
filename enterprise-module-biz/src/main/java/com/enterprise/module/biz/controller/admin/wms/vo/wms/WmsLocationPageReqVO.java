package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 库位分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsLocationPageReqVO extends PageParam {
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "库位编码")
    private String code;
    @Schema(description = "库位名称")
    private String name;
    @Schema(description = "库位类型（1存储区 2拣货区 3收货区 4退货区）")
    private Integer type;
    @Schema(description = "状态（0启用 1停用）")
    private Integer status;
}
