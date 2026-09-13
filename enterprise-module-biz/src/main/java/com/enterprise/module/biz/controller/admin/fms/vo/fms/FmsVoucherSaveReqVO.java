package com.enterprise.module.biz.controller.admin.fms.vo.fms;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 记账凭证新增/修改 Request VO")
@Data
public class FmsVoucherSaveReqVO {
    @Schema(description = "主键（更新时必填）")
    private Long id;
    @Schema(description = "凭证日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "凭证日期不能为空")
    private LocalDate voucherDate;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "分录列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分录不能为空")
    private List<Entry> entries;

    @Schema(description = "分录行")
    @Data
    public static class Entry {
        @Schema(description = "科目ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "科目不能为空")
        private Long accountId;
        @Schema(description = "摘要")
        private String summary;
        @Schema(description = "借方金额")
        private BigDecimal debitAmount;
        @Schema(description = "贷方金额")
        private BigDecimal creditAmount;
    }
}
