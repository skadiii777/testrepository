package com.enterprise.module.biz.dal.dataobject.payment;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 收付款流水 DO
 *
 * 销售单（已完成）登记收款、采购单（已完成）登记付款；
 * 流水只增不删改，累计金额不可超过关联单据总额。
 *
 * @author 企业管理平台
 */
@TableName("biz_payment")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentDO extends TenantBaseDO {

    /**
     * 主键
     */
    private Long id;
    /** Original receipt/payment reversed by this row. Null for ordinary/return entries. */
    private Long reversalOfId;
    /** Return document generating this entry. */
    private Long sourceReturnId;
    private String requestId;
    /**
     * 收付单号（SK/FK+时间戳）
     */
    private String paymentNo;
    /**
     * 收付类型（1=收款 2=付款）
     */
    private String paymentType;
    /**
     * 关联单据类型（1=销售单 2=采购单）
     */
    private String bizType;
    /**
     * 关联单据 id
     */
    private Long orderId;
    /**
     * 关联单据编号
     */
    private String orderCode;
    /**
     * 对方名称（客户/供应商）
     */
    private String partyName;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 收付方式（biz_payment_method：1现金 2银行转账 3微信 4支付宝）
     */
    private String paymentMethod;
    /**
     * 收付日期
     */
    private String paymentDate;
    /**
     * 关联合同 id（收款可选挂合同，用于合同回款进度；退货红冲流水也带此字段）
     */
    private Long contractId;
    /**
     * 备注
     */
    private String remark;

}
