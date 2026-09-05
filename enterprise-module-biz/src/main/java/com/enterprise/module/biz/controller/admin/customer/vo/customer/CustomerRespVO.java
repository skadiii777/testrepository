package com.enterprise.module.biz.controller.admin.customer.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户 Response VO")
@Data
public class CustomerRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "联系人")
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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}