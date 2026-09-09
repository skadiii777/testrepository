-- ============================================================
-- 部门默认角色映射管理页菜单（biz_dept_role_map 表与接口已在 register_apply.sql 中）
-- 页面：系统管理 → 部门角色映射（管理员专属：biz:dept-role-map:manage）
-- ============================================================

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '部门角色映射', 'biz:dept-role-map:manage', 2, 20, id, 'dept-role-map', 'ep:connection', 'system/deptRoleMap/index', 'SystemDeptRoleMap', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '系统管理' AND type = 1 LIMIT 1;
