package com.enterprise.module.biz.controller.admin.business.vo.business;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商机分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusinessPageReqVO extends PageParam {

    @Schema(description = "商机名称")
    private String name;

    @Schema(description = "关联客户名称")
    private String customerName;

    @Schema(description = "阶段（biz_business_stage）")
    private String stage;

}
