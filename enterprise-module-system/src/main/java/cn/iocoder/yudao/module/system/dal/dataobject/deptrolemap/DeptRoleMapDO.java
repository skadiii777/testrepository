package com.enterprise.module.system.dal.dataobject.deptrolemap;

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

/**
 * 部门 → 默认角色 映射 DO（预留架构）
 *
 * 注册审批通过时按申请部门读取该映射分配角色；未来开放新功能模块时，
 * 以部门/职位维度维护映射，即可将模块权限批量开放给对应用户。
 *
 * @author 企业管理平台
 */
@TableName("biz_dept_role_map")
@KeySequence("biz_dept_role_map_seq")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeptRoleMapDO extends TenantBaseDO {

    /** 主键 */
    private Long id;
    /** 部门编号 */
    private Long deptId;
    /** 角色编号 */
    private Long roleId;

}
