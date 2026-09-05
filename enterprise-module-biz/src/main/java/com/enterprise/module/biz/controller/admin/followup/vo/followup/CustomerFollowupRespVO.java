package com.enterprise.module.biz.controller.admin.followup.vo.followup;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户跟进 Response VO")
@Data
public class CustomerFollowupRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "客户名称")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "跟进时间")
    @ExcelProperty("跟进时间")
    private String followTime;

    @Schema(description = "跟进方式（1=电话 2=上门 3=微信 4=邮件 5=其他）")
    @ExcelProperty("跟进方式")
    private String method;

    @Schema(description = "跟进内容")
    private String content;

    @Schema(description = "下次跟进日期")
    private String nextDate;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
