package com.enterprise.module.biz.controller.admin.employee.vo.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 员工 Response VO")
@Data
public class EmployeeRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "工号")
    private String empNo;
    @Schema(description = "姓名")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}