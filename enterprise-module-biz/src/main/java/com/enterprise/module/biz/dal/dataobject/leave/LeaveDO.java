package com.enterprise.module.biz.dal.dataobject.leave;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 请假 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_leave")
@KeySequence("biz_leave_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**员工姓名*/
    private String empName;
    /**请假类型*/
    private String leaveType;
    /**开始日期*/
    private String startDate;
    /**结束日期*/
    private String endDate;
    /**请假天数*/
    private BigDecimal days;
    /**请假事由*/
    private String reason;
    /**审批状态*/
    private String status;
    /**审批意见*/
    private String remark;

}