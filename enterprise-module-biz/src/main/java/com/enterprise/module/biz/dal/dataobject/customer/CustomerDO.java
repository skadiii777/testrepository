package com.enterprise.module.biz.dal.dataobject.customer;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 客户 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_customer")
@KeySequence("biz_customer_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**客户名称*/
    private String customerName;
    /**联系人*/
    private String contactPerson;
    /**联系电话*/
    private String phone;
    /**邮箱*/
    private String email;
    /**所属行业*/
    private String industry;
    /**客户来源*/
    private String source;
    /**地址*/
    private String address;
    /**状态*/
    private String status;

}