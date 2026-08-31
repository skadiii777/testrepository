package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品对象 biz_product
 * 
 * @author biz
 */
public class Product extends BaseEntity
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
    /** 产品 */
    /** 产品编号 */
    @Excel(name = "产品编号")
    private String productCode;
    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;
    /** 产品分类 */
    @Excel(name = "产品分类")
    private String category;
    /** 单位 */
    @Excel(name = "单位")
    private String unit;
    /** 销售单价 */
    @Excel(name = "销售单价")
    private BigDecimal price;
    /** 成本价 */
    @Excel(name = "成本价")
    private BigDecimal cost;
    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Product() {
    }

    public void setProductCode(String productCode)
    {{
        this.productCode = productCode;
    }}

    public String getProductCode()
    {{
        return productCode;
    }}

    public void setProductName(String productName)
    {{
        this.productName = productName;
    }}

    public String getProductName()
    {{
        return productName;
    }}

    public void setCategory(String category)
    {{
        this.category = category;
    }}

    public String getCategory()
    {{
        return category;
    }}

    public void setUnit(String unit)
    {{
        this.unit = unit;
    }}

    public String getUnit()
    {{
        return unit;
    }}

    public void setPrice(BigDecimal price)
    {{
        this.price = price;
    }}

    public BigDecimal getPrice()
    {{
        return price;
    }}

    public void setCost(BigDecimal cost)
    {{
        this.cost = cost;
    }}

    public BigDecimal getCost()
    {{
        return cost;
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
            .append("productCode", getProductCode())
            .append("productName", getProductName())
            .append("category", getCategory())
            .append("unit", getUnit())
            .append("price", getPrice())
            .append("cost", getCost())
            .append("status", getStatus())
            .toString();
    }
}
