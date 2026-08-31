package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 合同对象 biz_contract
 * 
 * @author biz
 */
public class Contract extends BaseEntity
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
    /** 合同 */
    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractCode;
    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;
    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;
    /** 合同金额 */
    @Excel(name = "合同金额")
    private BigDecimal amount;
    /** 签订日期 */
    @Excel(name = "签订日期")
    private String signDate;
    /** 开始日期 */
    @Excel(name = "开始日期")
    private String startDate;
    /** 结束日期 */
    @Excel(name = "结束日期")
    private String endDate;
    /** 负责人 */
    @Excel(name = "负责人")
    private String owner;
    /** 合同状态 */
    @Excel(name = "合同状态", readConverterExp = "0=草稿,1=执行中,2=已完成,3=已终止")
    private String status;

    public Contract() {
    }

    public void setContractCode(String contractCode)
    {{
        this.contractCode = contractCode;
    }}

    public String getContractCode()
    {{
        return contractCode;
    }}

    public void setCustomerName(String customerName)
    {{
        this.customerName = customerName;
    }}

    public String getCustomerName()
    {{
        return customerName;
    }}

    public void setProductName(String productName)
    {{
        this.productName = productName;
    }}

    public String getProductName()
    {{
        return productName;
    }}

    public void setAmount(BigDecimal amount)
    {{
        this.amount = amount;
    }}

    public BigDecimal getAmount()
    {{
        return amount;
    }}

    public void setSignDate(String signDate)
    {{
        this.signDate = signDate;
    }}

    public String getSignDate()
    {{
        return signDate;
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

    public void setOwner(String owner)
    {{
        this.owner = owner;
    }}

    public String getOwner()
    {{
        return owner;
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
            .append("contractCode", getContractCode())
            .append("customerName", getCustomerName())
            .append("productName", getProductName())
            .append("amount", getAmount())
            .append("signDate", getSignDate())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("owner", getOwner())
            .append("status", getStatus())
            .toString();
    }
}
