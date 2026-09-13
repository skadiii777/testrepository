package com.enterprise.module.biz.controller.admin.wms.vo.wms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - WMS 作业任务分页 Request VO（type=putaway 上架 / pick 拣货）")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WmsTaskPageReqVO extends PageParam {
    @Schema(description = "仓库ID")
    private Long warehouseId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "状态（0进行中 1已完成）", example = "0")
    private Integer status;
}
