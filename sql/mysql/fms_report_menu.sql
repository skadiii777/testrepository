-- 财务报表菜单（企业管理 → FMS 财务 → 财务报表；幂等）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '财务报表', 'biz:fms:voucher:query', 2, 3, m.id, 'report', 'ep:trend-charts', 'biz/fms/report/index', 'BizFmsReport', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE m.permission = 'biz:fms:query' AND m.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.component = 'biz/fms/report/index' AND x.deleted = b'0')
LIMIT 1;
