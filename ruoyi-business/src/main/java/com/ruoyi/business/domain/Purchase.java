package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购单对象 biz_purchase
 * 
 * @author biz
 */
public class Purchase extends BaseEntity
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
    /** 采购单 */
    /** 采购单号 */
    @Excel(name = "采购单号")
    private String purchaseCode;
    /** 供应商 */
    @Excel(name = "供应商")
    private String supplierName;
    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;
    /** 采购数量 */
    @Excel(name = "采购数量")
    private Long quantity;
    /** 采购单价 */
    @Excel(name = "采购单价")
    private BigDecimal price;
    /** 总金额 */
    @Excel(name = "总金额")
    private BigDecimal totalAmount;
    /** 采购日期 */
    @Excel(name = "采购日期")
    private String purchaseDate;
    /** 入库状态 */
    @Excel(name = "入库状态", readConverterExp = "0=待处理,1=已完成")
    private String status;

    public Purchase() {
    }

    public void setPurchaseCode(String purchaseCode)
    {{
        this.purchaseCode = purchaseCode;
    }}

    public String getPurchaseCode()
    {{
        return purchaseCode;
    }}

    public void setSupplierName(String supplierName)
    {{
        this.supplierName = supplierName;
    }}

    public String getSupplierName()
    {{
        return supplierName;
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

    public void setPurchaseDate(String purchaseDate)
    {{
        this.purchaseDate = purchaseDate;
    }}

    public String getPurchaseDate()
    {{
        return purchaseDate;
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
            .append("purchaseCode", getPurchaseCode())
            .append("supplierName", getSupplierName())
            .append("productName", getProductName())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("totalAmount", getTotalAmount())
            .append("purchaseDate", getPurchaseDate())
            .append("status", getStatus())
            .toString();
    }
}
