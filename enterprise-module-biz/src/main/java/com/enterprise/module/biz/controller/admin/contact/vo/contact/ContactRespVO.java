package com.enterprise.module.biz.controller.admin.contact.vo.contact;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 客户联系人 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ContactRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联客户 id")
    private Long customerId;

    @Schema(description = "关联客户名称")
    @ExcelProperty("客户名称")
    private String customerName;

    @Schema(description = "联系人姓名")
    @ExcelProperty("联系人姓名")
    private String name;

    @Schema(description = "职位")
    @ExcelProperty("职位")
    private String position;

    @Schema(description = "手机号")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "邮箱")
    @ExcelProperty("邮箱")
    private String email;

    @Schema(description = "微信")
    private String wechat;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

}
