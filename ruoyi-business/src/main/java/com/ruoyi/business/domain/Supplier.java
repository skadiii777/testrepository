package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商对象 biz_supplier
 * 
 * @author biz
 */
public class Supplier extends BaseEntity
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
    /** 供应商 */
    /** 供应商名称 */
    @Excel(name = "供应商名称")
    private String supplierName;
    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;
    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;
    /** 地址 */
    @Excel(name = "地址")
    private String address;
    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Supplier() {
    }

    public void setSupplierName(String supplierName)
    {{
        this.supplierName = supplierName;
    }}

    public String getSupplierName()
    {{
        return supplierName;
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
            .append("supplierName", getSupplierName())
            .append("contactPerson", getContactPerson())
            .append("phone", getPhone())
            .append("address", getAddress())
            .append("status", getStatus())
            .toString();
    }
}
