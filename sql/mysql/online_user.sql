-- ============================================================
-- 登录安全：单账号单设备 + 在线用户管理
-- 后端：登录时清除同账号旧会话（access+refresh token）实现互踢；
--       /system/online-user/list|kick 在线检测与强制下线。
-- 前端令牌改存 sessionStorage（关页即失效），无表结构变更。
-- ============================================================

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '在线用户', 'system:online-user:query', 2, 21, id, 'online-user', 'ep:monitor', 'system/onlineUser/index', 'SystemOnlineUser', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '系统管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('在线用户强制下线', 'system:online-user:delete', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
