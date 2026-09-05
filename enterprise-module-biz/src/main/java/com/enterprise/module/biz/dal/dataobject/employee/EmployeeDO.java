package com.enterprise.module.biz.dal.dataobject.employee;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 员工 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_employee")
@KeySequence("biz_employee_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**工号*/
    private String empNo;
    /**姓名*/
    private String empName;
    /**部门*/
    private String deptName;
    /**岗位*/
    private String postName;
    /**联系电话*/
    private String phone;
    /**邮箱*/
    private String email;
    /**入职日期*/
    private String entryDate;
    /**状态*/
    private String status;

}