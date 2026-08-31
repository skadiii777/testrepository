package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考勤对象 biz_attendance
 * 
 * @author biz
 */
public class Attendance extends BaseEntity
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
    /** 考勤 */
    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String empName;
    /** 考勤日期 */
    @Excel(name = "考勤日期")
    private String workDate;
    /** 上班时间 */
    @Excel(name = "上班时间")
    private String checkIn;
    /** 下班时间 */
    @Excel(name = "下班时间")
    private String checkOut;
    /** 考勤状态 */
    @Excel(name = "考勤状态", readConverterExp = "0=正常,1=迟到,2=早退,3=缺勤")
    private String status;

    public Attendance() {
    }

    public void setEmpName(String empName)
    {{
        this.empName = empName;
    }}

    public String getEmpName()
    {{
        return empName;
    }}

    public void setWorkDate(String workDate)
    {{
        this.workDate = workDate;
    }}

    public String getWorkDate()
    {{
        return workDate;
    }}

    public void setCheckIn(String checkIn)
    {{
        this.checkIn = checkIn;
    }}

    public String getCheckIn()
    {{
        return checkIn;
    }}

    public void setCheckOut(String checkOut)
    {{
        this.checkOut = checkOut;
    }}

    public String getCheckOut()
    {{
        return checkOut;
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
            .append("workDate", getWorkDate())
            .append("checkIn", getCheckIn())
            .append("checkOut", getCheckOut())
            .append("status", getStatus())
            .toString();
    }
}
