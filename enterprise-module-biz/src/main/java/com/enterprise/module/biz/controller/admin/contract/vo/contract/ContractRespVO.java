package com.enterprise.module.biz.controller.admin.contract.vo.contract;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合同 Response VO")
@Data
public class ContractRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "合同编号")
    private String contractCode;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "合同金额")
    private BigDecimal amount;
    @Schema(description = "签订日期")
    private String signDate;
    @Schema(description = "开始日期")
    private String startDate;
    @Schema(description = "结束日期")
    private String endDate;
    @Schema(description = "负责人")
    private String owner;
    @Schema(description = "合同状态")
    private String status;

    @Schema(description = "已回款金额（收款流水挂合同的汇总）")
    private java.math.BigDecimal receivedAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}