package com.enterprise.module.biz.dal.dataobject.attendance;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 考勤 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_attendance")
@KeySequence("biz_attendance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**员工姓名*/
    private String empName;
    /**考勤日期*/
    private String workDate;
    /**上班时间*/
    private String checkIn;
    /**下班时间*/
    private String checkOut;
    /**考勤状态*/
    private String status;
    /**加班时长（分钟）*/
    private Integer overtimeMinutes;

}