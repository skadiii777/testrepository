package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存流水对象 biz_stock_move
 *
 * 参考 Odoo stock.move：每次出入库记录一条不可变流水
 * （产品/仓库/方向/数量/结余/来源单号/操作人）
 *
 * @author biz
 */
public class StockMove extends BaseEntity
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

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;

    /** 仓库 */
    @Excel(name = "仓库")
    private String warehouse;

    /** 类型（1入库 2出库） */
    @Excel(name = "类型", readConverterExp = "1=入库,2=出库")
    private String moveType;

    /** 本次数量（正数） */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 操作后结余 */
    @Excel(name = "结余")
    private BigDecimal balanceAfter;

    /** 来源类型（purchase/sales/manual） */
    @Excel(name = "来源类型")
    private String sourceType;

    /** 来源单号 */
    @Excel(name = "来源单号")
    private String sourceCode;

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setWarehouse(String warehouse)
    {
        this.warehouse = warehouse;
    }

    public String getWarehouse()
    {
        return warehouse;
    }

    public void setMoveType(String moveType)
    {
        this.moveType = moveType;
    }

    public String getMoveType()
    {
        return moveType;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public void setBalanceAfter(BigDecimal balanceAfter)
    {
        this.balanceAfter = balanceAfter;
    }

    public BigDecimal getBalanceAfter()
    {
        return balanceAfter;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceCode(String sourceCode)
    {
        this.sourceCode = sourceCode;
    }

    public String getSourceCode()
    {
        return sourceCode;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("productName", getProductName())
            .append("warehouse", getWarehouse())
            .append("moveType", getMoveType())
            .append("quantity", getQuantity())
            .append("balanceAfter", getBalanceAfter())
            .append("sourceType", getSourceType())
            .append("sourceCode", getSourceCode())
            .toString();
    }
}
