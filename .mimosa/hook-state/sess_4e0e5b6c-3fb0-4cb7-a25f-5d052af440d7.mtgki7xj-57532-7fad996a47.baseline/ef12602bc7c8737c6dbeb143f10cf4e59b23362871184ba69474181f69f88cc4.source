package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 员工对象 biz_employee
 * 
 * @author biz
 */
public class Employee extends BaseEntity
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
    /** 员工 */
    /** 工号 */
    @Excel(name = "工号")
    private String empNo;
    /** 姓名 */
    @Excel(name = "姓名")
    private String empName;
    /** 部门 */
    @Excel(name = "部门")
    private String deptName;
    /** 岗位 */
    @Excel(name = "岗位")
    private String postName;
    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;
    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;
    /** 入职日期 */
    @Excel(name = "入职日期")
    private String entryDate;
    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Employee() {
    }

    public void setEmpNo(String empNo)
    {{
        this.empNo = empNo;
    }}

    public String getEmpNo()
    {{
        return empNo;
    }}

    public void setEmpName(String empName)
    {{
        this.empName = empName;
    }}

    public String getEmpName()
    {{
        return empName;
    }}

    public void setDeptName(String deptName)
    {{
        this.deptName = deptName;
    }}

    public String getDeptName()
    {{
        return deptName;
    }}

    public void setPostName(String postName)
    {{
        this.postName = postName;
    }}

    public String getPostName()
    {{
        return postName;
    }}

    public void setPhone(String phone)
    {{
        this.phone = phone;
    }}

    public String getPhone()
    {{
        return phone;
    }}

    public void setEmail(String email)
    {{
        this.email = email;
    }}

    public String getEmail()
    {{
        return email;
    }}

    public void setEntryDate(String entryDate)
    {{
        this.entryDate = entryDate;
    }}

    public String getEntryDate()
    {{
        return entryDate;
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
            .append("empNo", getEmpNo())
            .append("empName", getEmpName())
            .append("deptName", getDeptName())
            .append("postName", getPostName())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("entryDate", getEntryDate())
            .append("status", getStatus())
            .toString();
    }
}
