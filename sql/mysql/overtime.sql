-- ----------------------------
-- 加班时长功能（v5 功能补全 #2）
-- 考勤表增加加班分钟字段；下班打卡晚于 18:00 / 补下班卡晚于 18:00 时自动累计
-- ----------------------------
ALTER TABLE biz_attendance
  ADD COLUMN `overtime_minutes` int DEFAULT 0 COMMENT '加班时长（分钟）' AFTER `status`;

-- 考勤月报菜单（人事考勤 → 考勤月报）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '考勤月报', 'biz:attendance:query', 2, 5, id, 'summary', '#', 'biz/attendance/summary', 'AttendanceSummary', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '人事考勤' AND type = 1 LIMIT 1;
