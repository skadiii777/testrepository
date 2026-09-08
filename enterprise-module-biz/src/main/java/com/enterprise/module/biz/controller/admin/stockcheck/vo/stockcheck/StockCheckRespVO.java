package com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 库存盘点 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StockCheckRespVO {

    @Schema(description = "主键")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "盘点单号")
    @ExcelProperty("盘点单号")
    private String checkNo;

    @Schema(description = "产品名称")
    @ExcelProperty("产品名称")
    private String productName;

    @Schema(description = "仓库")
    @ExcelProperty("仓库")
    private String warehouse;

    @Schema(description = "账面数量")
    @ExcelProperty("账面数量")
    private Long bookQuantity;

    @Schema(description = "实盘数量")
    @ExcelProperty("实盘数量")
    private Long actualQuantity;

    @Schema(description = "差异（实盘-账面）")
    @ExcelProperty("差异")
    private Long diffQuantity;

    @Schema(description = "状态（0=待确认 1=已确认）")
    private String status;

    @Schema(description = "盘点日期")
    @ExcelProperty("盘点日期")
    private String checkDate;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

}
