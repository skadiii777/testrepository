package com.enterprise.module.biz.controller.admin.stockmove.vo.stockmove;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.enterprise.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 库存流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StockMovePageReqVO extends PageParam {

    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "仓库")
    private String warehouse;
    @Schema(description = "类型")
    private String moveType;
    @Schema(description = "来源类型")
    private String sourceType;
    @Schema(description = "来源单号")
    private String sourceCode;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}