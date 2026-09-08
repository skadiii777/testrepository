-- =====================================================================
-- 普通角色（role_id=2, code=common）权限收窄脚本
-- 背景：ruoyi-vue-pro.sql 基础种子给普通角色授了全部演示菜单（系统管理/
--       基础设施/流程管理/监控中心等），普通员工不应看到管理端模块。
-- 目标授权集：员工工作台目录 + 5 个页面菜单 + 5 个按钮菜单，共 11 项。
-- 用法：基础库 + 业务库导入后执行本脚本（幂等，可重复执行）。
-- 注意：仅清理 tenant_id=1 的授权行；执行后需清权限缓存或重启服务
--       （缓存 key 形如 menu_role_ids:1:{menuId}、user_role_ids:{userId}）。
-- =====================================================================

-- 1) 回收普通角色在租户 1 下的全部授权
DELETE FROM system_role_menu WHERE role_id = 2 AND tenant_id = 1;

-- 2) 重新授予：员工工作台目录（按名字定位，兼容不同环境自增 id）
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 2, id, '1', NOW(), '1', NOW(), b'0', 1 FROM system_menu
WHERE name = '员工工作台' AND type = 1 AND deleted = 0;

-- 3) 重新授予：工作台页面与按钮权限
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 2, id, '1', NOW(), '1', NOW(), b'0', 1 FROM system_menu
WHERE deleted = 0 AND status = 0 AND (permission LIKE 'portal:%');

-- 4) 校验：应恰好 11 行（1 目录 + 5 页面 + 5 按钮）
SELECT COUNT(*) AS common_role_grants FROM system_role_menu
WHERE role_id = 2 AND deleted = 0 AND tenant_id = 1;
