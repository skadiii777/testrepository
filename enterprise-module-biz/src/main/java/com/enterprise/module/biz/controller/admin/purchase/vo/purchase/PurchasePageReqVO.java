package com.enterprise.module.biz.controller.admin.purchase.vo.purchase;

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.enterprise.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 采购单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PurchasePageReqVO extends PageParam {

    @Schema(description = "采购单号")
    private String purchaseCode;
    @Schema(description = "产品名称")
    private Long productId;
    private Long warehouseId;
    private String warehouse;
    private String productName;
    @Schema(description = "采购日期")
    private String purchaseDate;
    @Schema(description = "入库状态")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}