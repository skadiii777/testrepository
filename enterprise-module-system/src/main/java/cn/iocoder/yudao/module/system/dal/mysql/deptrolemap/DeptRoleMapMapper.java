package com.enterprise.module.system.dal.mysql.deptrolemap;

import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.system.dal.dataobject.deptrolemap.DeptRoleMapDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 部门 → 默认角色 映射 Mapper（预留架构）
 *
 * @author 企业管理平台
 */
@Mapper
public interface DeptRoleMapMapper extends BaseMapperX<DeptRoleMapDO> {

    default List<DeptRoleMapDO> selectListByDept(Long deptId) {
        return selectList(new LambdaQueryWrapperX<DeptRoleMapDO>()
                .eq(DeptRoleMapDO::getDeptId, deptId));
    }

    default DeptRoleMapDO selectByDeptAndRole(Long deptId, Long roleId) {
        return selectOne(new LambdaQueryWrapperX<DeptRoleMapDO>()
                .eq(DeptRoleMapDO::getDeptId, deptId)
                .eq(DeptRoleMapDO::getRoleId, roleId));
    }

}
