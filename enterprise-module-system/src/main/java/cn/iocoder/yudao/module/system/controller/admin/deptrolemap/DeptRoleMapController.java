package com.enterprise.module.system.controller.admin.deptrolemap;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.module.system.dal.dataobject.deptrolemap.DeptRoleMapDO;
import com.enterprise.module.system.dal.mysql.deptrolemap.DeptRoleMapMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;

/**
 * 部门 → 默认角色 映射（预留接口）
 *
 * 当前用途：注册审批通过时按申请部门分配默认角色。
 * 未来扩展：新功能模块上线后，以部门/职位维度维护该映射，
 * 即可将模块权限批量开放给对应用户，无需逐个授权。
 */
@Tag(name = "管理后台 - 部门默认角色映射")
@RestController
@RequestMapping("/system/dept-role-map")
@Validated
public class DeptRoleMapController {

    @Resource
    private DeptRoleMapMapper deptRoleMapMapper;

    @GetMapping("/list-by-dept")
    @Operation(summary = "获得部门的默认角色映射列表")
    @Parameter(name = "deptId", description = "部门编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:dept-role-map:manage')")
    public CommonResult<List<DeptRoleMapDO>> getListByDept(@RequestParam("deptId") Long deptId) {
        return success(deptRoleMapMapper.selectListByDept(deptId));
    }

    @PostMapping("/create")
    @Operation(summary = "创建部门默认角色映射")
    @PreAuthorize("@ss.hasPermission('biz:dept-role-map:manage')")
    public CommonResult<Long> create(@RequestParam("deptId") Long deptId,
                                     @RequestParam("roleId") Long roleId) {
        DeptRoleMapDO exist = deptRoleMapMapper.selectByDeptAndRole(deptId, roleId);
        if (exist != null) {
            return success(exist.getId());
        }
        DeptRoleMapDO map = DeptRoleMapDO.builder().deptId(deptId).roleId(roleId).build();
        deptRoleMapMapper.insert(map);
        return success(map.getId());
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除部门默认角色映射")
    @Parameter(name = "id", description = "映射编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:dept-role-map:manage')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        deptRoleMapMapper.deleteById(id);
        return success(true);
    }

}
