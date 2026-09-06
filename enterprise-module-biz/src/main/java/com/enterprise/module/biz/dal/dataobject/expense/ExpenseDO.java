package com.enterprise.module.biz.dal.dataobject.expense;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 费用报销 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_expense")
@KeySequence("biz_expense_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**报销人*/
    private String empName;
    /**费用类别*/
    private String category;
    /**金额*/
    private BigDecimal amount;
    /**费用发生日期*/
    private String expenseDate;
    /**费用说明*/
    private String reason;
    /**发票附件URL*/
    private String invoiceUrl;
    /**审批状态*/
    private String status;
    /**流程实例编号（BPM）*/
    private String processInstanceId;
    /**审批意见*/
    private String auditRemark;
    /**审批人*/
    private String auditBy;
    /**审批时间*/
    private String auditTime;

}