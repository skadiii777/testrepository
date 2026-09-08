package com.enterprise.module.system.controller.admin.registerapply.vo.registerapply;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 注册申请 Response VO")
@Data
public class RegisterApplyRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "申请部门")
    private Long deptId;

    @Schema(description = "申请岗位")
    private Long postId;

    @Schema(description = "状态（0=待审批 1=已通过 2=已驳回）")
    private String status;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审批时间")
    private String auditTime;

    @Schema(description = "申请时间")
    private String createTime;

}
