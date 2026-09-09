package com.enterprise.module.biz.controller.admin.contact.vo.contact;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客户联系人分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContactPageReqVO extends PageParam {

    @Schema(description = "关联客户 id")
    private Long customerId;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "联系人姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

}
