package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 记账凭证 Response VO（含分录明细）")
@Data
public class FmsVoucherRespVO {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "凭证号")
    private String voucherNo;
    @Schema(description = "凭证日期")
    private LocalDate voucherDate;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "状态（0草稿 1已记账）")
    private Integer status;
    @Schema(description = "借方合计")
    private BigDecimal debitTotal;
    @Schema(description = "贷方合计")
    private BigDecimal creditTotal;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "分录明细列表")
    private List<Entry> entries;

    @Schema(description = "分录行")
    @Data
    public static class Entry {
        private Long id;
        private Long accountId;
        private String accountCode;
        private String accountName;
        private String summary;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
