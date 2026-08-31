package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存对象 biz_stock
 * 
 * @author biz
 */
public class Stock extends BaseEntity
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
    /** 库存 */
    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;
    /** 仓库 */
    @Excel(name = "仓库")
    private String warehouse;
    /** 库存数量 */
    @Excel(name = "库存数量")
    private Long quantity;
    /** 预警下限 */
    @Excel(name = "预警下限")
    private Long minQuantity;

    public Stock() {
    }

    public void setProductName(String productName)
    {{
        this.productName = productName;
    }}

    public String getProductName()
    {{
        return productName;
    }}

    public void setWarehouse(String warehouse)
    {{
        this.warehouse = warehouse;
    }}

    public String getWarehouse()
    {{
        return warehouse;
    }}

    public void setQuantity(Long quantity)
    {{
        this.quantity = quantity;
    }}

    public Long getQuantity()
    {{
        return quantity;
    }}

    public void setMinQuantity(Long minQuantity)
    {{
        this.minQuantity = minQuantity;
    }}

    public Long getMinQuantity()
    {{
        return minQuantity;
    }}

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("productName", getProductName())
            .append("warehouse", getWarehouse())
            .append("quantity", getQuantity())
            .append("minQuantity", getMinQuantity())
            .toString();
    }
}
