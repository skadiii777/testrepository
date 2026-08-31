package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户对象 biz_customer
 * 
 * @author biz
 */
public class Customer extends BaseEntity
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
    /** 客户 */
    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;
    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;
    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;
    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;
    /** 所属行业 */
    @Excel(name = "所属行业")
    private String industry;
    /** 客户来源 */
    @Excel(name = "客户来源")
    private String source;
    /** 地址 */
    @Excel(name = "地址")
    private String address;
    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Customer() {
    }

    public void setCustomerName(String customerName)
    {{
        this.customerName = customerName;
    }}

    public String getCustomerName()
    {{
        return customerName;
    }}

    public void setContactPerson(String contactPerson)
    {{
        this.contactPerson = contactPerson;
    }}

    public String getContactPerson()
    {{
        return contactPerson;
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

    public void setIndustry(String industry)
    {{
        this.industry = industry;
    }}

    public String getIndustry()
    {{
        return industry;
    }}

    public void setSource(String source)
    {{
        this.source = source;
    }}

    public String getSource()
    {{
        return source;
    }}

    public void setAddress(String address)
    {{
        this.address = address;
    }}

    public String getAddress()
    {{
        return address;
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
            .append("customerName", getCustomerName())
            .append("contactPerson", getContactPerson())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("industry", getIndustry())
            .append("source", getSource())
            .append("address", getAddress())
            .append("status", getStatus())
            .toString();
    }
}
