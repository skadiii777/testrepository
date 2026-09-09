package com.enterprise.module.biz.controller.admin.clue.vo.clue;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 销售线索 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ClueRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "线索名称")
    @ExcelProperty("线索名称")
    private String name;

    @Schema(description = "联系人")
    @ExcelProperty("联系人")
    private String contactName;

    @Schema(description = "联系电话")
    @ExcelProperty("联系电话")
    private String contactMobile;

    @Schema(description = "线索来源")
    @ExcelProperty("线索来源")
    private String source;

    @Schema(description = "状态")
    @ExcelProperty("状态")
    private String status;

    @Schema(description = "负责人")
    @ExcelProperty("负责人")
    private String ownerName;

    @Schema(description = "转化后的客户 id")
    private Long customerId;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
