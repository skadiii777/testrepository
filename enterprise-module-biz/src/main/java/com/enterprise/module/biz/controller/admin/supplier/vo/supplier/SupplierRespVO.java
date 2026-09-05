package com.enterprise.module.biz.controller.admin.supplier.vo.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 供应商 Response VO")
@Data
public class SupplierRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "供应商名称")
    private String supplierName;
    @Schema(description = "联系人")
    private String contactPerson;
    @Schema(description = "联系电话")
    private String phone;
    @Schema(description = "地址")
    private String address;
    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}