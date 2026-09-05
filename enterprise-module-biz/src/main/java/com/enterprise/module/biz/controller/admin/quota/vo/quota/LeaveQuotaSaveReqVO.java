package com.enterprise.module.biz.controller.admin.quota.vo.quota;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 假期余额新增/修改 Request VO")
@Data
public class LeaveQuotaSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "员工姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="员工姓名不能为空")
    private String empName;
    @Schema(description = "假期类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="假期类型不能为空")
    private String leaveType;
    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="年份不能为空")
    private String year;
    @Schema(description = "配额天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="配额天数不能为空")
    private BigDecimal quotaDays;
    @Schema(description = "已用天数")
    
    private BigDecimal usedDays;
}