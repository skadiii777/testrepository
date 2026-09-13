package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 会计科目新增/修改 Request VO")
@Data
public class FmsAccountSaveReqVO {
    @Schema(description = "主键（更新时必填）")
    private Long id;
    @Schema(description = "科目编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "科目编码不能为空")
    private String code;
    @Schema(description = "科目名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "科目名称不能为空")
    private String name;
    @Schema(description = "科目类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "科目类型不能为空")
    private Integer type;
    @Schema(description = "余额方向（1借 2贷）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "余额方向不能为空")
    private Integer direction;
    @Schema(description = "上级科目ID")
    private Long parentId;
    @Schema(description = "状态（0启用 1停用）")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
}
