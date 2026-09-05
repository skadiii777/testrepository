package com.enterprise.module.biz.controller.admin.attendance.vo.attendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 考勤 Response VO")
@Data
public class AttendanceRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "员工姓名")
    private String empName;
    @Schema(description = "考勤日期")
    private String workDate;
    @Schema(description = "上班时间")
    private String checkIn;
    @Schema(description = "下班时间")
    private String checkOut;
    @Schema(description = "考勤状态")
    private String status;

    @Schema(description = "加班时长（分钟）")
    private Integer overtimeMinutes;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}