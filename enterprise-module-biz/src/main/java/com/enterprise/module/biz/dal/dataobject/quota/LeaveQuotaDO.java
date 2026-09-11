package com.enterprise.module.biz.dal.dataobject.quota;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 假期余额 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_leave_quota")
@KeySequence("biz_leave_quota_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveQuotaDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**员工姓名*/
    private Long employeeId;
    private String empName;
    /**假期类型*/
    private String leaveType;
    /**年份*/
    private String year;
    /**配额天数*/
    private BigDecimal quotaDays;
    /**已用天数*/
    private BigDecimal usedDays;

    /** 剩余额度（配额-已用） */
    public java.math.BigDecimal getRemainDays() {{
        if (quotaDays == null) {{
            return java.math.BigDecimal.ZERO;
        }}
        return quotaDays.subtract(usedDays == null ? java.math.BigDecimal.ZERO : usedDays);
 }}
}