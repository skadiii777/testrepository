package com.enterprise.module.biz.controller.admin.leave.vo.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 请假 Response VO")
@Data
public class LeaveRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "员工姓名")
    private String empName;
    @Schema(description = "请假类型")
    private String leaveType;
    @Schema(description = "开始日期")
    private String startDate;
    @Schema(description = "结束日期")
    private String endDate;
    @Schema(description = "请假天数")
    private BigDecimal days;
    @Schema(description = "请假事由")
    private String reason;
    @Schema(description = "审批状态")
    private String status;
    @Schema(description = "审批意见")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}