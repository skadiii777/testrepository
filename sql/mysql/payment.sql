-- ============================================================
-- 收付款管理（对应 yudao ERP finance 收付款单 / CRM 回款概念）
-- 销售单（已完成）登记收款，采购单（已完成）登记付款；
-- 累计金额不可超过单据总额；流水只增不删改（删除仅限管理员权限）。
-- 执行前提：enterprise-biz.sql / order_status.sql 已导入。
-- ============================================================

-- 1. 表
CREATE TABLE biz_payment (
  `id`             bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payment_no`     varchar(32)   NOT NULL                COMMENT '收付单号（SK/FK+时间戳）',
  `payment_type`   char(1)       NOT NULL                COMMENT '收付类型（1=收款 2=付款）',
  `biz_type`       char(1)       NOT NULL                COMMENT '关联单据类型（1=销售单 2=采购单）',
  `order_id`       bigint        NOT NULL                COMMENT '关联单据 id',
  `order_code`     varchar(64)   NOT NULL                COMMENT '关联单据编号',
  `party_name`     varchar(128)  DEFAULT NULL            COMMENT '对方名称（客户/供应商）',
  `amount`         decimal(12,2) NOT NULL                COMMENT '金额',
  `payment_method` char(1)       DEFAULT NULL            COMMENT '收付方式（biz_payment_method：1现金 2银行转账 3微信 4支付宝）',
  `payment_date`   varchar(20)   NOT NULL                COMMENT '收付日期',
  `remark`         varchar(500)  DEFAULT NULL            COMMENT '备注',
  `creator`        varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`        varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`    datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        bit(1)        NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`      bigint        NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`biz_type`, `order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='收付款流水表';

-- 2. 字典
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('收付类型', 'biz_payment_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '收款', '1', 'biz_payment_type', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '付款', '2', 'biz_payment_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('收付方式', 'biz_payment_method', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '现金', '1', 'biz_payment_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '银行转账', '2', 'biz_payment_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '微信', '3', 'biz_payment_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '支付宝', '4', 'biz_payment_method', 0, '1', NOW(), '1', NOW(), b'0');

-- 3. 管理端菜单：企业管理 → 收付款管理（超管免授权自动可见）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '收付款管理', 'biz:payment:query', 2, 6, id, 'payment', 'ep:money', 'biz/payment/index', 'BizPayment', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '企业管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('收付款新增', 'biz:payment:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('收付款删除', 'biz:payment:delete', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
