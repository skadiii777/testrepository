package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 费用报销对象 biz_expense
 *
 * 参考 Odoo hr_expense：员工提交 -> 审批 -> 统计
 *
 * @author biz
 */
public class Expense extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    /** 报销人 */
    @Excel(name = "报销人")
    private String empName;

    /** 费用类别（1差旅 2餐费 3办公 4其他） */
    @Excel(name = "费用类别", readConverterExp = "1=差旅,2=餐费,3=办公,4=其他")
    private String category;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 费用发生日期 */
    @Excel(name = "费用日期")
    private String expenseDate;

    /** 费用说明 */
    private String reason;

    /** 审批状态（0待审批 1已通过 2已驳回） */
    @Excel(name = "审批状态", readConverterExp = "0=待审批,1=已通过,2=已驳回")
    private String status;

    /** 审批意见 */
    private String auditRemark;

    /** 审批人 */
    private String auditBy;

    /** 审批时间 */
    private String auditTime;

    public void setEmpName(String empName)
    {
        this.empName = empName;
    }

    public String getEmpName()
    {
        return empName;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getCategory()
    {
        return category;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setExpenseDate(String expenseDate)
    {
        this.expenseDate = expenseDate;
    }

    public String getExpenseDate()
    {
        return expenseDate;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditTime(String auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getAuditTime()
    {
        return auditTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("empName", getEmpName())
            .append("category", getCategory())
            .append("amount", getAmount())
            .append("expenseDate", getExpenseDate())
            .append("status", getStatus())
            .toString();
    }
}
