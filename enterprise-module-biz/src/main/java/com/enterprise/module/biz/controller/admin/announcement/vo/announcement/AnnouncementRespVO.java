package com.enterprise.module.biz.controller.admin.announcement.vo.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 公司公告 Response VO")
@Data
public class AnnouncementRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型（biz_announcement_type）")
    private String type;

    @Schema(description = "正文")
    private String content;

    @Schema(description = "是否置顶（0=否 1=是）")
    private String pinned;

    @Schema(description = "状态（0=已发布 1=已下架）")
    private String status;

    @Schema(description = "发布日期")
    private String publishDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
