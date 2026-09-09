package com.enterprise.module.biz.controller.admin.business.vo.business;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商机 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusinessRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "商机名称")
    @ExcelProperty("商机名称")
    private String name;

    @Schema(description = "关联客户名称")
    @ExcelProperty("关联客户")
    private String customerName;

    @Schema(description = "阶段")
    @ExcelProperty("阶段")
    private String stage;

    @Schema(description = "预期金额")
    @ExcelProperty("预期金额")
    private BigDecimal amount;

    @Schema(description = "预计成交日期")
    @ExcelProperty("预计成交日期")
    private String expectedDate;

    @Schema(description = "负责人")
    @ExcelProperty("负责人")
    private String ownerName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
