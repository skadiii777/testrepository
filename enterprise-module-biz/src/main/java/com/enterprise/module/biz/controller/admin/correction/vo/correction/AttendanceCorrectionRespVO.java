package com.enterprise.module.biz.controller.admin.correction.vo.correction;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 补卡申请 Response VO")
@Data
public class AttendanceCorrectionRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "员工姓名")
    @ExcelProperty("员工姓名")
    private String empName;

    @Schema(description = "补卡日期")
    @ExcelProperty("补卡日期")
    private String workDate;

    @Schema(description = "补卡类型（1=补上班卡 2=补下班卡）")
    @ExcelProperty("补卡类型")
    private String correctType;

    @Schema(description = "补卡时间")
    @ExcelProperty("补卡时间")
    private String correctTime;

    @Schema(description = "补卡原因")
    private String reason;

    @Schema(description = "审批状态（0待审批 1已通过 2已驳回）")
    private String status;

    @Schema(description = "审批意见")
    private String auditRemark;

    @Schema(description = "审批人")
    private String auditBy;

    @Schema(description = "审批时间")
    private String auditTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
