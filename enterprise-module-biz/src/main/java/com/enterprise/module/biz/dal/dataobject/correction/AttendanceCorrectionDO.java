package com.enterprise.module.biz.dal.dataobject.correction;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 补卡申请 DO
 *
 * 审批通过后自动回写考勤记录
 *
 * @author 企业管理平台
 */
@TableName("biz_attendance_correction")
@KeySequence("biz_attendance_correction_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**员工姓名*/
    private String empName;
    /**补卡日期*/
    private String workDate;
    /**补卡类型（1=补上班卡 2=补下班卡）*/
    private String correctType;
    /**补卡时间（HH:mm）*/
    private String correctTime;
    /**补卡原因*/
    private String reason;
    /**审批状态（0待审批 1已通过 2已驳回）*/
    private String status;
    /**审批意见*/
    private String auditRemark;
    /**审批人*/
    private String auditBy;
    /**审批时间*/
    private String auditTime;
}
