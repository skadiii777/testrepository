package com.enterprise.module.biz.controller.admin.contact.vo.contact;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 客户联系人新增/修改 Request VO")
@Data
public class ContactSaveReqVO {

    @Schema(description = "主键（更新时必填）")
    private Long id;

    @Schema(description = "关联客户 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联客户不能为空")
    private Long customerId;

    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "联系人姓名不能为空")
    private String name;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "微信")
    private String wechat;

    @Schema(description = "备注")
    private String remark;

}
