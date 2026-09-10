package com.enterprise.module.biz.dal.dataobject.contract;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 合同 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_contract")
@KeySequence("biz_contract_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**合同编号*/
    private String contractCode;
    /**客户名称*/
    private String customerName;
    /**产品名称*/
    private String productName;
    /**合同金额*/
    private BigDecimal amount;
    /**签订日期*/
    private String signDate;
    /**开始日期*/
    private String startDate;
    /**结束日期*/
    private String endDate;
    /**负责人*/
    private String owner;
    /**合同状态*/
    private String status;
    /**备注（如转化来源）*/
    private String remark;

    /**
     * 已回款金额（非表字段：由收付款流水按 contract_id 汇总填充）
     */
    @TableField(exist = false)
    private BigDecimal receivedAmount;

}