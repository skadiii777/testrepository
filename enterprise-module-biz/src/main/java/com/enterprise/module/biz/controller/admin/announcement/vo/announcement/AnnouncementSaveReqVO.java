package com.enterprise.module.biz.controller.admin.announcement.vo.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Schema(description = "管理后台 - 公司公告新增/修改 Request VO")
@Data
public class AnnouncementSaveReqVO {

    @Schema(description = "主键（更新时必填）")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "公告标题不能为空")
    private String title;

    @Schema(description = "类型（biz_announcement_type：1通知 2公告 3制度）")
    private String type;

    @Schema(description = "正文", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "公告正文不能为空")
    private String content;

    @Schema(description = "是否置顶（0=否 1=是）")
    private String pinned;

    @Schema(description = "状态（0=已发布 1=已下架）")
    private String status;

}
