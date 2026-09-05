package com.enterprise.module.biz.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 员工新增/修改 Request VO")
@Data
public class EmployeeSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "工号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="工号不能为空")
    private String empNo;
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="姓名不能为空")
    private String empName;
    @Schema(description = "部门")
    
    private String deptName;
    @Schema(description = "岗位")
    
    private String postName;
    @Schema(description = "联系电话")
    
    private String phone;
    @Schema(description = "邮箱")
    
    private String email;
    @Schema(description = "入职日期")
    
    private String entryDate;
    @Schema(description = "状态")
    
    private String status;
}