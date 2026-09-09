-- ============================================================
-- 客户联系人（对齐 yudao CRM 联系人概念，简化版）
-- 一个客户可挂多个联系人（决策人/经办人）；线索转商机时自动落入。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 表
CREATE TABLE biz_contact (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `customer_id`   bigint       NOT NULL                COMMENT '关联客户 id',
  `customer_name` varchar(128) NOT NULL                COMMENT '关联客户名称（冗余）',
  `name`          varchar(64)  NOT NULL                COMMENT '联系人姓名',
  `position`      varchar(64)  DEFAULT NULL            COMMENT '职位',
  `mobile`        varchar(32)  NOT NULL                COMMENT '手机号',
  `email`         varchar(128) DEFAULT NULL            COMMENT '邮箱',
  `wechat`        varchar(64)  DEFAULT NULL            COMMENT '微信',
  `remark`        varchar(500) DEFAULT NULL            COMMENT '备注',
  `creator`       varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`       varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       bit(1)       NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`     bigint       NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='客户联系人表';

-- 2. 管理端菜单：企业管理 → 客户合同产品 → 联系人管理（管理员专属）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '联系人管理', 'biz:contact:query', 2, 4, id, 'contact', 'ep:user', 'biz/contact/index', 'BizContact', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '客户合同产品' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('联系人新增', 'biz:contact:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('联系人更新', 'biz:contact:update', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('联系人删除', 'biz:contact:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
