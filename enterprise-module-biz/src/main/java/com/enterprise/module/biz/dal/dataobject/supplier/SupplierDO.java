package com.enterprise.module.biz.dal.dataobject.supplier;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 供应商 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_supplier")
@KeySequence("biz_supplier_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**供应商名称*/
    private String supplierName;
    /**联系人*/
    private String contactPerson;
    /**联系电话*/
    private String phone;
    /**地址*/
    private String address;
    /**状态*/
    private String status;

}