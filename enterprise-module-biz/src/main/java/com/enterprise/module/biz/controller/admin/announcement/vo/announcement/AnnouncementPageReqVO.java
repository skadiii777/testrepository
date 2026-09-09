package com.enterprise.module.biz.controller.admin.announcement.vo.announcement;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 公司公告分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AnnouncementPageReqVO extends PageParam {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "类型（biz_announcement_type）")
    private String type;

    @Schema(description = "状态（0=已发布 1=已下架）")
    private String status;

}
