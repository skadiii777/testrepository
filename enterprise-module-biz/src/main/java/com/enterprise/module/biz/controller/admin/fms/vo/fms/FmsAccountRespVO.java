package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会计科目 Response VO")
@Data
public class FmsAccountRespVO {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "科目编码")
    private String code;
    @Schema(description = "科目名称")
    private String name;
    @Schema(description = "科目类型")
    private Integer type;
    @Schema(description = "余额方向")
    private Integer direction;
    @Schema(description = "上级科目ID")
    private Long parentId;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
