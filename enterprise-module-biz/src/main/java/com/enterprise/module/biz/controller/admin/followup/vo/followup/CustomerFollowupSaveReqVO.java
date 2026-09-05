package com.enterprise.module.biz.controller.admin.followup.vo.followup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 客户跟进新增 Request VO")
@Data
public class CustomerFollowupSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @Schema(description = "跟进时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "跟进时间不能为空")
    private String followTime;

    @Schema(description = "跟进方式（1=电话 2=上门 3=微信 4=邮件 5=其他）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "跟进方式不能为空")
    private String method;

    @Schema(description = "跟进内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "跟进内容不能为空")
    private String content;

    @Schema(description = "下次跟进日期")
    private String nextDate;
}
