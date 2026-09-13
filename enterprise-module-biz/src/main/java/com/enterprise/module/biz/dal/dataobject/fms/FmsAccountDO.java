package com.enterprise.module.biz.dal.dataobject.fms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会计科目 DO
 */
@TableName("biz_fms_account")
@KeySequence("biz_fms_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsAccountDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 科目编码（1001/1002/2202...） */
    private String code;
    /** 科目名称 */
    private String name;
    /** 科目类型（1资产 2负债 3权益 4成本 5损益） */
    private Integer type;
    /** 余额方向（1借方 2贷方） */
    private Integer direction;
    /** 上级科目ID（0=一级） */
    private Long parentId;
    /** 状态（0启用 1停用） */
    private Integer status;
    /** 备注 */
    private String remark;

}
