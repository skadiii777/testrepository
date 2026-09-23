-- AI 知识库菜单启用（2026-09-23 · RAG 首期上生产）
-- 依据 docs/AI-MODULE-INTEGRATION.md：首期仅上线「AI 知识库」。
-- 上游其余 AI demo 菜单（写作/音乐/思维导图等）前端组件未随系统迁移，保持停用避免 404。
-- 幂等：重复执行无副作用。仅动 791 与 850-854，其余 791 子菜单（若存在）不动。

UPDATE `system_menu` SET `status` = 0
WHERE `id` IN (791, 850, 851, 852, 853, 854)
  AND `deleted` = 0;
