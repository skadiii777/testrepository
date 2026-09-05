package com.enterprise.module.biz.controller.admin.customer.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 客户新增/修改 Request VO")
@Data
public class CustomerSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="客户名称不能为空")
    private String customerName;
    @Schema(description = "联系人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="联系人不能为空")
    private String contactPerson;
    @Schema(description = "联系电话")
    
    private String phone;
    @Schema(description = "邮箱")
    
    private String email;
    @Schema(description = "所属行业")
    
    private String industry;
    @Schema(description = "客户来源")
    
    private String source;
    @Schema(description = "地址")
    
    private String address;
    @Schema(description = "状态")
    
    private String status;
}