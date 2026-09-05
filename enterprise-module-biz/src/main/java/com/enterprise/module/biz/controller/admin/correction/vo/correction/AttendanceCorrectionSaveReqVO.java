package com.enterprise.module.biz.controller.admin.correction.vo.correction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 补卡申请新增/修改 Request VO")
@Data
public class AttendanceCorrectionSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "员工姓名，员工自助提交时由服务端填充")
    private String empName;

    @Schema(description = "补卡日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "补卡日期不能为空")
    private String workDate;

    @Schema(description = "补卡类型（1=补上班卡 2=补下班卡）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "补卡类型不能为空")
    private String correctType;

    @Schema(description = "补卡时间（HH:mm）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "补卡时间不能为空")
    private String correctTime;

    @Schema(description = "补卡原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "补卡原因不能为空")
    private String reason;
}
