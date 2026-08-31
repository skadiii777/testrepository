package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 请假对象 biz_leave
 * 
 * @author biz
 */
public class Leave extends BaseEntity
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
    /** 请假 */
    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String empName;
    /** 请假类型 */
    @Excel(name = "请假类型", readConverterExp = "1=事假,2=病假,3=年假,4=调休")
    private String leaveType;
    /** 开始日期 */
    @Excel(name = "开始日期")
    private String startDate;
    /** 结束日期 */
    @Excel(name = "结束日期")
    private String endDate;
    /** 请假天数 */
    @Excel(name = "请假天数")
    private BigDecimal days;
    /** 请假事由 */
    @Excel(name = "请假事由")
    private String reason;
    /** 审批状态 */
    @Excel(name = "审批状态", readConverterExp = "0=待审批,1=已通过,2=已驳回")
    private String status;

    public Leave() {
    }

    public void setEmpName(String empName)
    {{
        this.empName = empName;
    }}

    public String getEmpName()
    {{
        return empName;
    }}

    public void setLeaveType(String leaveType)
    {{
        this.leaveType = leaveType;
    }}

    public String getLeaveType()
    {{
        return leaveType;
    }}

    public void setStartDate(String startDate)
    {{
        this.startDate = startDate;
    }}

    public String getStartDate()
    {{
        return startDate;
    }}

    public void setEndDate(String endDate)
    {{
        this.endDate = endDate;
    }}

    public String getEndDate()
    {{
        return endDate;
    }}

    public void setDays(BigDecimal days)
    {{
        this.days = days;
    }}

    public BigDecimal getDays()
    {{
        return days;
    }}

    public void setReason(String reason)
    {{
        this.reason = reason;
    }}

    public String getReason()
    {{
        return reason;
    }}

    public void setStatus(String status)
    {{
        this.status = status;
    }}

    public String getStatus()
    {{
        return status;
    }}

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("empName", getEmpName())
            .append("leaveType", getLeaveType())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("days", getDays())
            .append("reason", getReason())
            .append("status", getStatus())
            .toString();
    }
}
