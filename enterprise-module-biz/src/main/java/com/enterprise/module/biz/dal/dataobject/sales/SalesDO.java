package com.enterprise.module.biz.dal.dataobject.sales;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 销售单 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_sales")
@KeySequence("biz_sales_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**销售单号*/
    private String salesCode;
    /**客户*/
    private String customerName;
    /**产品名称*/
    private Long productId;
    private Long warehouseId;
    private String warehouse;
    private String productName;
    /**销售数量*/
    private Long quantity;
    /**销售单价*/
    private BigDecimal price;
    /**总金额*/
    private BigDecimal totalAmount;
    /**销售日期*/
    private String salesDate;
    /**出库状态*/
    private String status;

}