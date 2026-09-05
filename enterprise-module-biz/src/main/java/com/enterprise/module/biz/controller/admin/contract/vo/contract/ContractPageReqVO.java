package com.enterprise.module.biz.controller.admin.contract.vo.contract;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.enterprise.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 合同分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ContractPageReqVO extends PageParam {

    @Schema(description = "合同编号")
    private String contractCode;
    @Schema(description = "客户名称")
    private String customerName;
    @Schema(description = "签订日期")
    private String signDate;
    @Schema(description = "负责人")
    private String owner;
    @Schema(description = "合同状态")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}