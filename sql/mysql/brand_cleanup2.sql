-- ============================================================
-- 去品牌化收尾（2026-09-13）：运行时可见的 yudao 残留
-- 来源：全量页面 DOM 巡检（101 菜单页 + 8 静态路由）
-- 幂等版：可重复执行。
-- ============================================================

-- 1. 用户头像死链（admin 头像指向 test.yudao.iocoder.cn，全站顶栏裂图）
UPDATE system_users SET avatar = '' WHERE avatar LIKE '%yudao%' AND deleted = 0;

-- 2. OAuth2 应用管理页可见的芋道演示数据
--    id=1 是登录在用的 default 应用：改名 + 清 logo（不动 client_id/secret）
UPDATE system_oauth2_client SET name = '企业平台', logo = '' WHERE id = 1 AND deleted = 0;
--    id=40/41/42 为上游 SSO 教学演示应用，停用（保留行可回滚）
UPDATE system_oauth2_client SET deleted = 1, updater = '1', update_time = NOW() WHERE id IN (40, 41, 42) AND deleted = 0;

-- 3. 链路追踪菜单停用（服务器未部署 SkyWalking，页面会 iframe 加载上游外站）
UPDATE system_menu SET status = 1, updater = '1', update_time = NOW()
WHERE id = 85 AND deleted = 0;
