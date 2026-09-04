package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 假期余额对象 biz_leave_quota
 *
 * 员工 × 假期类型 × 年份 的配额与已用天数，
 * 参考 Odoo hr_holidays 的 allocation 设计
 *
 * @author biz
 */
public class LeaveQuota extends BaseEntity
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

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String empName;

    /** 假期类型（1事假 2病假 3年假 4调休） */
    @Excel(name = "假期类型", readConverterExp = "1=事假,2=病假,3=年假,4=调休")
    private String leaveType;

    /** 年份 */
    @Excel(name = "年份")
    private String year;

    /** 配额天数 */
    @Excel(name = "配额天数")
    private BigDecimal quotaDays;

    /** 已用天数 */
    @Excel(name = "已用天数")
    private BigDecimal usedDays;

    public void setEmpName(String empName)
    {
        this.empName = empName;
    }

    public String getEmpName()
    {
        return empName;
    }

    public void setLeaveType(String leaveType)
    {
        this.leaveType = leaveType;
    }

    public String getLeaveType()
    {
        return leaveType;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getYear()
    {
        return year;
    }

    public void setQuotaDays(BigDecimal quotaDays)
    {
        this.quotaDays = quotaDays;
    }

    public BigDecimal getQuotaDays()
    {
        return quotaDays;
    }

    public void setUsedDays(BigDecimal usedDays)
    {
        this.usedDays = usedDays;
    }

    public BigDecimal getUsedDays()
    {
        return usedDays;
    }

    /** 剩余额度（配额-已用），无记录视为0 */
    public BigDecimal getRemainDays()
    {
        if (quotaDays == null)
        {
            return BigDecimal.ZERO;
        }
        return quotaDays.subtract(usedDays == null ? BigDecimal.ZERO : usedDays);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("empName", getEmpName())
            .append("leaveType", getLeaveType())
            .append("year", getYear())
            .append("quotaDays", getQuotaDays())
            .append("usedDays", getUsedDays())
            .toString();
    }
}
