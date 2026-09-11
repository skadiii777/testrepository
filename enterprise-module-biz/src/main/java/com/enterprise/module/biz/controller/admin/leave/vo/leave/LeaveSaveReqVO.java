package com.enterprise.module.biz.controller.admin.leave.vo.leave;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 请假新增/修改 Request VO")
@Data
public class LeaveSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "员工姓名")
    
    private Long employeeId;
    private String empName;
    @Schema(description = "请假类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="请假类型不能为空")
    private String leaveType;
    @Schema(description = "开始日期")
    
    private String startDate;
    @Schema(description = "结束日期")
    
    private String endDate;
    @Schema(description = "请假天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="请假天数不能为空")
    @jakarta.validation.constraints.Positive(message="请假天数必须大于0")
    private BigDecimal days;
    @Schema(description = "请假事由")
    
    private String reason;
    @Schema(description = "审批状态")
    
    private String status;

    @Schema(description = "流程实例编号（BPM，服务端填充）")
    private String processInstanceId;
    @Schema(description = "审批意见")
    
    private String remark;
}
