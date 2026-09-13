package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 会计科目分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FmsAccountPageReqVO extends PageParam {
    @Schema(description = "科目编码")
    private String code;
    @Schema(description = "科目名称")
    private String name;
    @Schema(description = "科目类型（1资产 2负债 3权益 4成本 5损益）")
    private Integer type;
    @Schema(description = "状态（0启用 1停用）")
    private Integer status;
}
