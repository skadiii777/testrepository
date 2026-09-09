-- ============================================================
-- CRM 线索 + 商机（销售漏斗，对齐 yudao CRM Clue/Business 简化版）
-- 线索跟进后一键转化为 客户 + 商机；商机按 6 阶段推进漏斗（5赢单/6输单终局）。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 表
CREATE TABLE biz_clue (
  `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`           varchar(128) NOT NULL                COMMENT '线索名称（客户公司名称）',
  `contact_name`   varchar(64)  NOT NULL                COMMENT '联系人',
  `contact_mobile` varchar(32)  NOT NULL                COMMENT '联系电话',
  `source`         char(1)      DEFAULT NULL            COMMENT '线索来源（biz_clue_source：1广告投放 2客户推荐 3官网咨询 4电话营销 5其他渠道）',
  `status`         char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（biz_clue_status：0待跟进 1跟进中 2已转化 3已无效）',
  `owner_name`     varchar(64)  DEFAULT NULL            COMMENT '负责人',
  `customer_id`    bigint       DEFAULT NULL            COMMENT '转化后的客户 id',
  `remark`         varchar(500) DEFAULT NULL            COMMENT '备注',
  `creator`        varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`        varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        bit(1)       NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`      bigint       NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='销售线索表';

CREATE TABLE biz_business (
  `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`          varchar(128)  NOT NULL                COMMENT '商机名称',
  `customer_name` varchar(128)  NOT NULL                COMMENT '关联客户名称',
  `stage`         char(1)       NOT NULL DEFAULT '1'    COMMENT '阶段（biz_business_stage：1初步接触 2需求确认 3方案报价 4谈判协商 5赢单 6输单）',
  `amount`        decimal(12,2) DEFAULT NULL            COMMENT '预期金额',
  `expected_date` varchar(20)   DEFAULT NULL            COMMENT '预计成交日期',
  `owner_name`    varchar(64)   DEFAULT NULL            COMMENT '负责人',
  `remark`        varchar(500)  DEFAULT NULL            COMMENT '备注',
  `creator`       varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`       varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       bit(1)        NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`     bigint        NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='商机表';

-- 2. 字典（system_dict_data 为全局表，无 tenant_id 列）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('线索来源', 'biz_clue_source', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '广告投放', '1', 'biz_clue_source', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '客户推荐', '2', 'biz_clue_source', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '官网咨询', '3', 'biz_clue_source', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '电话营销', '4', 'biz_clue_source', 0, '1', NOW(), '1', NOW(), b'0'),
       (5, '其他渠道', '5', 'biz_clue_source', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('线索状态', 'biz_clue_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '待跟进', '0', 'biz_clue_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '跟进中', '1', 'biz_clue_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '已转化', '2', 'biz_clue_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '已无效', '3', 'biz_clue_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('商机阶段', 'biz_business_stage', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '初步接触', '1', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '需求确认', '2', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '方案报价', '3', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '谈判协商', '4', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0'),
       (5, '赢单', '5', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0'),
       (6, '输单', '6', 'biz_business_stage', 0, '1', NOW(), '1', NOW(), b'0');

-- 3. 管理端菜单：企业管理 → 线索管理 / 商机管理（管理员专属；system_menu 为全局表）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '线索管理', 'biz:clue:query', 2, 8, id, 'clue', 'fa:filter', 'biz/clue/index', 'BizClue', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '企业管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('线索新增', 'biz:clue:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('线索更新', 'biz:clue:update', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('线索删除', 'biz:clue:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '商机管理', 'biz:business:query', 2, 9, id, 'business', 'fa:line-chart', 'biz/business/index', 'BizBusiness', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '企业管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('商机新增', 'biz:business:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('商机更新', 'biz:business:update', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('商机删除', 'biz:business:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
