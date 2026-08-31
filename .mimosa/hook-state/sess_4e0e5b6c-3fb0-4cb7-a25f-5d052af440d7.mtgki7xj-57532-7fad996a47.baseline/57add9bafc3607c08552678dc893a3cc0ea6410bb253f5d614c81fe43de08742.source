package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 销售单对象 biz_sales
 * 
 * @author biz
 */
public class Sales extends BaseEntity
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
    /** 销售单 */
    /** 销售单号 */
    @Excel(name = "销售单号")
    private String salesCode;
    /** 客户 */
    @Excel(name = "客户")
    private String customerName;
    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;
    /** 销售数量 */
    @Excel(name = "销售数量")
    private Long quantity;
    /** 销售单价 */
    @Excel(name = "销售单价")
    private BigDecimal price;
    /** 总金额 */
    @Excel(name = "总金额")
    private BigDecimal totalAmount;
    /** 销售日期 */
    @Excel(name = "销售日期")
    private String salesDate;
    /** 出库状态 */
    @Excel(name = "出库状态", readConverterExp = "0=待处理,1=已完成")
    private String status;

    public Sales() {
    }

    public void setSalesCode(String salesCode)
    {{
        this.salesCode = salesCode;
    }}

    public String getSalesCode()
    {{
        return salesCode;
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

    public void setQuantity(Long quantity)
    {{
        this.quantity = quantity;
    }}

    public Long getQuantity()
    {{
        return quantity;
    }}

    public void setPrice(BigDecimal price)
    {{
        this.price = price;
    }}

    public BigDecimal getPrice()
    {{
        return price;
    }}

    public void setTotalAmount(BigDecimal totalAmount)
    {{
        this.totalAmount = totalAmount;
    }}

    public BigDecimal getTotalAmount()
    {{
        return totalAmount;
    }}

    public void setSalesDate(String salesDate)
    {{
        this.salesDate = salesDate;
    }}

    public String getSalesDate()
    {{
        return salesDate;
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
            .append("salesCode", getSalesCode())
            .append("customerName", getCustomerName())
            .append("productName", getProductName())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("totalAmount", getTotalAmount())
            .append("salesDate", getSalesDate())
            .append("status", getStatus())
            .toString();
    }
}
