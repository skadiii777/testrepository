-- ----------------------------
-- 经营周报摘要（v5 功能补全 #5）
-- 每周一 09:00 通过站内信推送给管理员
-- ----------------------------

-- 站内信模板（type=2 系统消息；content 支持 {var} 占位）
INSERT INTO system_notify_template (name, code, nickname, content, type, params, status, creator, create_time, updater, update_time, deleted)
VALUES ('经营周报摘要', 'biz_weekly_digest', '企业管理平台',
        '上周经营摘要：新增客户 {customerCount} 家；销售单 {salesCount} 张（金额 {salesAmount} 元）；采购单 {purchaseCount} 张（金额 {purchaseAmount} 元）；当前待审批：请假 {leavePending} 条、报销 {expensePending} 条、补卡 {correctionPending} 条。',
        2, '["customerCount","salesCount","salesAmount","purchaseCount","purchaseAmount","leavePending","expensePending","correctionPending"]', 0, '1', NOW(), '1', NOW(), b'0');

-- 定时任务：每周一 09:00（handler 为 biz 周报 Job 的 Bean 名）
INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time)
VALUES ('经营周报摘要', 1, 'bizWeeklyDigestJob', '', '0 0 9 ? * MON', 0, 0, 0, '1', NOW(), '1', NOW());
