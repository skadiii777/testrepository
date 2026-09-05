-- ----------------------------
-- 补卡申请表（v5 功能补全 #1）
-- ----------------------------
DROP TABLE IF EXISTS biz_attendance_correction;
CREATE TABLE biz_attendance_correction (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `emp_name`      varchar(50)  DEFAULT NULL           COMMENT '员工姓名',
  `work_date`     varchar(20)  DEFAULT NULL           COMMENT '补卡日期',
  `correct_type`  char(1)      DEFAULT NULL           COMMENT '补卡类型（1=补上班卡 2=补下班卡）',
  `correct_time`  varchar(10)  DEFAULT NULL           COMMENT '补卡时间（HH:mm）',
  `reason`        varchar(500) DEFAULT NULL           COMMENT '补卡原因',
  `status`        char(1)      DEFAULT '0'            COMMENT '审批状态（0待审批 1已通过 2已驳回）',
  `audit_remark`  varchar(500) DEFAULT NULL           COMMENT '审批意见',
  `audit_by`      varchar(64)  DEFAULT NULL           COMMENT '审批人',
  `audit_time`    varchar(20)  DEFAULT NULL           COMMENT '审批时间',
  `creator`       varchar(64)  DEFAULT ''             COMMENT '创建者',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`       varchar(64)  DEFAULT ''             COMMENT '更新者',
  `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       bit(1)       NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  `tenant_id`     bigint       NOT NULL DEFAULT 0     COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='补卡申请表';

-- 字典
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('补卡类型', 'biz_correction_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '补上班卡', '1', 'biz_correction_type', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '补下班卡', '2', 'biz_correction_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('补卡审批状态', 'biz_correction_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '待审批', '0', 'biz_correction_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '已通过', '1', 'biz_correction_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '已驳回', '2', 'biz_correction_status', 0, '1', NOW(), '1', NOW(), b'0');

-- 管理端菜单：协作审批 → 补卡申请
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '补卡申请', 'biz:correction:query', 2, 3, id, 'correction', '#', 'biz/correction/index', 'AttendanceCorrection', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '协作审批' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('补卡查询', 'biz:correction:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('补卡审批', 'biz:correction:audit', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('补卡删除', 'biz:correction:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('补卡导出', 'biz:correction:export', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');

-- 门户菜单：员工工作台 → 我的补卡
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '我的补卡', 'portal:correction:query', 2, 5, id, 'correction', '#', 'portal/correction/index', 'PortalCorrection', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '员工工作台' AND type = 1 LIMIT 1;
SET @pm = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('补卡提交', 'portal:correction:add', 3, 1, @pm, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');

-- 普通角色授权
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted)
SELECT 2, id, '1', NOW(), '1', NOW(), b'0' FROM system_menu WHERE permission LIKE 'portal:correction:%';
