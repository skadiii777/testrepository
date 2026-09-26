-- 审批事件站内信模板（2026-09-26 · 审批联动）
-- 提交 → 通知审批人；审批结果 → 通知申请人。幂等。
-- 注意：system_notify_template.params 必须是 JSON 数组格式（JacksonTypeHandler
-- 反序列化为 List<String>），逗号分隔字符串会在发送时抛 JsonParseException。

INSERT INTO `system_notify_template` (`name`,`code`,`nickname`,`content`,`type`,`params`,`status`,`remark`,`creator`,`updater`,`deleted`)
SELECT '审批待办提醒','biz_approval_pending','审批助手','【审批待办】{applicant} 提交了{type}申请：{summary}，请前往审批中心处理。',3,'["type","applicant","summary"]',0,'审批提交→通知审批人','1','1','\0'
WHERE NOT EXISTS (SELECT 1 FROM `system_notify_template` WHERE code='biz_approval_pending' AND deleted=0);

INSERT INTO `system_notify_template` (`name`,`code`,`nickname`,`content`,`type`,`params`,`status`,`remark`,`creator`,`updater`,`deleted`)
SELECT '审批结果通知','biz_approval_result','审批助手','【审批结果】你提交的{type}申请已{result}。备注：{remark}',3,'["type","result","remark"]',0,'审批结果→通知申请人','1','1','\0'
WHERE NOT EXISTS (SELECT 1 FROM `system_notify_template` WHERE code='biz_approval_result' AND deleted=0);
