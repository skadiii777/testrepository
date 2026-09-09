package com.enterprise.module.biz.controller.admin.clue.vo.clue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 销售线索新增/修改 Request VO")
@Data
public class ClueSaveReqVO {

    @Schema(description = "主键（更新时必填）")
    private Long id;

    @Schema(description = "线索名称（客户公司名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "线索名称不能为空")
    private String name;

    @Schema(description = "联系人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "联系人不能为空")
    private String contactName;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "联系电话不能为空")
    private String contactMobile;

    @Schema(description = "线索来源（biz_clue_source）")
    private String source;

    @Schema(description = "状态（仅更新时可改：0待跟进 1跟进中 3已无效；已转化由转化接口负责）")
    private String status;

    @Schema(description = "备注")
    private String remark;

}
