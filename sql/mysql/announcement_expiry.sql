-- ============================================================
-- 公司公告 + 到期提醒（办公协同小批量）
-- 公告：管理员发布、全员工作台可见；到期提醒：每日 09:00 扫描
-- 执行中合同到期（30/7/1/0 天）与超期未成交商机（周一），站内信通知管理员。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 公告表
CREATE TABLE biz_announcement (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        varchar(200) NOT NULL                COMMENT '标题',
  `type`         char(1)      DEFAULT NULL            COMMENT '类型（biz_announcement_type：1通知 2公告 3制度）',
  `content`      text         NOT NULL                COMMENT '正文',
  `pinned`       char(1)      NOT NULL DEFAULT '0'    COMMENT '是否置顶（0=否 1=是）',
  `status`       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0=已发布 1=已下架）',
  `publish_date` varchar(20)  DEFAULT NULL            COMMENT '发布日期',
  `creator`      varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`      varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      bit(1)       NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`    bigint       NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`, `pinned`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='公司公告表';

-- 2. 公告类型字典（全局表）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('公告类型', 'biz_announcement_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '通知', '1', 'biz_announcement_type', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '公告', '2', 'biz_announcement_type', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '制度', '3', 'biz_announcement_type', 0, '1', NOW(), '1', NOW(), b'0');

-- 3. 菜单：协作审批 → 公告管理（管理员专属管理端）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '公告管理', 'biz:announcement:query', 2, 6, id, 'announcement', 'ep:bell', 'biz/announcement/index', 'BizAnnouncement', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '协作审批' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('公告新增', 'biz:announcement:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('公告更新', 'biz:announcement:update', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('公告删除', 'biz:announcement:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');

-- 4. 到期提醒站内信模板（params 为 JSON 数组格式）
INSERT INTO system_notify_template (name, code, nickname, content, type, params, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('到期提醒', 'biz_expiry_reminder', '系统提醒',
        '到期提醒：{content}', 2,
        '["content"]', 0, '合同到期与商机超期自动提醒', '1', NOW(), '1', NOW(), b'0');

-- 5. 定时任务：每日 09:00 扫描（handler_name 对应 Bean bizExpiryReminderJob）
INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, create_time, updater, update_time, deleted)
VALUES ('到期提醒（合同/商机）', 1, 'bizExpiryReminderJob', '', '0 0 9 * * ?', 0, 0, 0, '1', NOW(), '1', NOW(), b'0');
