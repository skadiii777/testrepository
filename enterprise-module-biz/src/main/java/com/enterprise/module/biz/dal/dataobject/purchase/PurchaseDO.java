package com.enterprise.module.biz.dal.dataobject.purchase;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 采购单 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_purchase")
@KeySequence("biz_purchase_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**采购单号*/
    private String purchaseCode;
    /**供应商*/
    private String supplierName;
    /**产品名称*/
    private String productName;
    /**采购数量*/
    private Long quantity;
    /**采购单价*/
    private BigDecimal price;
    /**总金额*/
    private BigDecimal totalAmount;
    /**采购日期*/
    private String purchaseDate;
    /**入库状态*/
    private String status;

}