package com.enterprise.module.biz.controller.admin.quota.vo.quota;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 假期余额 Response VO")
@Data
public class LeaveQuotaRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "员工姓名")
    private Long employeeId;
    private String empName;
    @Schema(description = "假期类型")
    private String leaveType;
    @Schema(description = "年份")
    private String year;
    @Schema(description = "配额天数")
    private BigDecimal quotaDays;
    @Schema(description = "已用天数")
    private BigDecimal usedDays;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}