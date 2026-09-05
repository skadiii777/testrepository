package com.enterprise.module.biz.controller.admin.supplier.vo.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 供应商新增/修改 Request VO")
@Data
public class SupplierSaveReqVO {

    @Schema(description = "主键，更新时必填")
    private Long id;

    @Schema(description = "供应商名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="供应商名称不能为空")
    private String supplierName;
    @Schema(description = "联系人")
    
    private String contactPerson;
    @Schema(description = "联系电话")
    
    private String phone;
    @Schema(description = "地址")
    
    private String address;
    @Schema(description = "状态")
    
    private String status;
}