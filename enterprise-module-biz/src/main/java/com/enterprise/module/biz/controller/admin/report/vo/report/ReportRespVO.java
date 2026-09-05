package com.enterprise.module.biz.controller.admin.report.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 业务汇报 Response VO")
@Data
public class ReportRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "汇报类型")
    private String reportType;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "汇报内容")
    private String content;
    @Schema(description = "汇报日期")
    private String reportDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}