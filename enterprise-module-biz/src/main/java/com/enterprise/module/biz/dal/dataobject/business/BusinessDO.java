package com.enterprise.module.biz.dal.dataobject.business;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 商机 DO
 *
 * 对齐 yudao CRM 商机概念（简化版）：挂在客户下，按阶段推进销售漏斗；
 * 阶段 5=赢单 / 6=输单 为终局，置入后不可再修改。
 *
 * @author 企业管理平台
 */
@TableName("biz_business")
@KeySequence("biz_business_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商机名称
     */
    private String name;
    /**
     * 关联客户名称
     */
    private String customerName;
    /**
     * 阶段（biz_business_stage：1初步接触 2需求确认 3方案报价 4谈判协商 5赢单 6输单）
     */
    private String stage;
    /**
     * 预期金额
     */
    private BigDecimal amount;
    /**
     * 预计成交日期
     */
    private String expectedDate;
    /**
     * 负责人（创建时自动取当前登录人昵称）
     */
    private String ownerName;
    /**
     * 备注
     */
    private String remark;

}
