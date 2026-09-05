package com.enterprise.module.biz.controller.admin.attendance.vo.attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 考勤新增/修改 Request VO")
@Data
public class AttendanceSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "员工姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="员工姓名不能为空")
    private String empName;
    @Schema(description = "考勤日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="考勤日期不能为空")
    private String workDate;
    @Schema(description = "上班时间")
    
    private String checkIn;
    @Schema(description = "下班时间")
    
    private String checkOut;
    @Schema(description = "考勤状态")
    
    private String status;

    @Schema(description = "加班时长（分钟）")
    private Integer overtimeMinutes;
}