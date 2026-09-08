-- =====================================================================
-- 修复：前端 v-hasPermi 引用了 5 个未落菜单表的权限串，导致对应按钮
-- 对所有角色（含超管）都被隐藏。本脚本按页面 component 定位父菜单补齐
-- 类型 3（按钮）菜单，幂等（NOT EXISTS 防重）。
-- 审计基线：grep 前端 v-hasPermi 权限串 vs system_menu.permission 全集。
-- =====================================================================

-- 1) 审批中心：审批操作（请假/报销/补卡三 Tab 共用）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '审批操作', 'biz:approval:audit', 3, 9, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE m.component = 'biz/approval/index' AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:approval:audit' AND x.deleted = 0);

-- 2) 补卡申请：新增 / 修改
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.perm, 3, t.sort, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
JOIN (SELECT '补卡新增' AS name, 'biz:correction:create' AS perm, 1 AS sort
      UNION ALL SELECT '补卡修改', 'biz:correction:update', 2) t
WHERE m.component = 'biz/correction/index' AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = t.perm AND x.deleted = 0);

-- 3) 客户跟进：修改
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '跟进修改', 'biz:followup:update', 3, 4, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE m.component = 'biz/followup/index' AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:followup:update' AND x.deleted = 0);

-- 4) 库存盘点：发起盘点
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '盘点新增', 'biz:stockcheck:create', 3, 3, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE m.component = 'biz/stockcheck/index' AND m.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:stockcheck:create' AND x.deleted = 0);

-- 5) 校验：以下查询应各返回 1 行
SELECT name, permission FROM system_menu WHERE permission IN
('biz:approval:audit', 'biz:correction:create', 'biz:correction:update', 'biz:followup:update', 'biz:stockcheck:create');
