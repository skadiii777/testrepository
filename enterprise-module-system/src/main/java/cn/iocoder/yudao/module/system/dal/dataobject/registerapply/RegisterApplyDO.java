package com.enterprise.module.system.dal.dataobject.registerapply;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 注册申请 DO
 *
 * 注册时创建（账号不落 system_users、不可登录），管理员审批通过后
 * 才创建正式账号并加入申请的部门/岗位、绑定角色；驳回则保留申请记录。
 *
 * @author 企业管理平台
 */
@TableName("biz_register_apply")
@KeySequence("biz_register_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RegisterApplyDO extends TenantBaseDO {

    /** 主键 */
    private Long id;
    /** 用户账号 */
    private String username;
    /** 密码（BCrypt 摘要；审批通过/驳回后置空） */
    private String password;
    /** 用户昵称 */
    private String nickname;
    /** 申请部门 */
    private Long deptId;
    /** 申请岗位 */
    private Long postId;
    /** 状态（0=待审批 1=已通过 2=已驳回） */
    private String status;
    /** 驳回原因 */
    private String rejectReason;
    /** 审批时间 */
    private String auditTime;

}
