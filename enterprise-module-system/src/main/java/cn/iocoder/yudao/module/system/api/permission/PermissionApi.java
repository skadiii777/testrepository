package com.enterprise.module.system.api.permission;

import com.enterprise.framework.common.biz.system.permission.PermissionCommonApi;

import java.util.Collection;
import java.util.Set;

/**
 * 权限 API 接口
 *
 * @author 企业管理平台源码
 */
public interface PermissionApi extends PermissionCommonApi {

    /**
     * 获得拥有多个角色的用户编号集合
     *
     * @param roleIds 角色编号集合
     * @return 用户编号集合
     */
    Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds);

}
