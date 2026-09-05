package com.enterprise.module.biz.controller.admin.report.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 业务汇报新增/修改 Request VO")
@Data
public class ReportSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "汇报类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="汇报类型不能为空")
    private String reportType;
    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="标题不能为空")
    private String title;
    @Schema(description = "汇报内容")
    
    private String content;
    @Schema(description = "汇报日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="汇报日期不能为空")
    private String reportDate;
}