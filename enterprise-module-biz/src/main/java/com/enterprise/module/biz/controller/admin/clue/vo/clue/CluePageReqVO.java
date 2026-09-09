package com.enterprise.module.biz.controller.admin.clue.vo.clue;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 销售线索分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CluePageReqVO extends PageParam {

    @Schema(description = "线索名称")
    private String name;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "线索来源（biz_clue_source）")
    private String source;

    @Schema(description = "状态（biz_clue_status）")
    private String status;

}
