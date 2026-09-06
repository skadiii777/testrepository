-- ----------------------------
-- 请假接入 Flowable 工作流（v6 #1）
-- ----------------------------
ALTER TABLE biz_leave
  ADD COLUMN `process_instance_id` varchar(64) DEFAULT NULL COMMENT '流程实例编号' AFTER `status`;

-- 请假审批流程模型（BPMN：发起人 -> 部门经理(admin) 审批）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '请假流程配置', 'bpm:model:query', 3, 9, m.id, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m WHERE m.name = '请假' AND m.type = 2 LIMIT 1;
