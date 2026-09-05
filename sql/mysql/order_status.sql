-- ----------------------------
-- 单据状态机（v5 功能补全 #3）
-- 采购/销售单：0草稿 → 1已确认 → 2已完成（库存联动点）；0/1 可作废为 3
-- ----------------------------

-- 新字典：单据状态
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('单据状态', 'biz_order_status', 0, '采购/销售单状态', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '草稿', '0', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '已确认', '1', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '已完成', '2', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '已作废', '3', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0');

-- 停用旧字典
UPDATE system_dict_type SET status = 1 WHERE type = 'biz_inout_status' AND deleted = 0;

-- 存量数据迁移：0待处理→0草稿；1已完成→2已完成
UPDATE biz_purchase SET status = '2' WHERE status = '1';
UPDATE biz_sales SET status = '2' WHERE status = '1';

-- 流转按钮权限（挂在采购单/销售单菜单下）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '确认'), 'biz:purchase:confirm', 3, 6, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '采购单' AND m.type = 2 LIMIT 1;
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '完成'), 'biz:purchase:complete', 3, 7, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '采购单' AND m.type = 2 LIMIT 1;
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '作废'), 'biz:purchase:void', 3, 8, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '采购单' AND m.type = 2 LIMIT 1;
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '确认'), 'biz:sales:confirm', 3, 6, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '销售单' AND m.type = 2 LIMIT 1;
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '完成'), 'biz:sales:complete', 3, 7, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '销售单' AND m.type = 2 LIMIT 1;
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT concat(m.name, '作废'), 'biz:sales:void', 3, 8, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '销售单' AND m.type = 2 LIMIT 1;
