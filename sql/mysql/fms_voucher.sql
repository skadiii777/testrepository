-- ============================================================
-- FMS 财务管理 Batch 1：会计科目 + 记账凭证 + 分录
-- 标准借贷记账法：凭证分录借方合计 = 贷方合计
-- 幂等版：表/字典/科目/菜单均可重复执行（已存在则跳过）。
-- 执行前提：enterprise-biz.sql 已导入（system_menu 含「企业管理」目录）。
-- 注意：yudao 上游自带停用的旧 FMS 演示菜单树（path=/fms, status=1，
--       含 id=1894），本脚本菜单以 permission 判重，勿按 path 挂靠。
-- ============================================================

-- 1. 会计科目表
CREATE TABLE IF NOT EXISTS biz_fms_account (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '科目编码（如 1001）',
  `name` varchar(128) NOT NULL COMMENT '科目名称（如 库存现金）',
  `type` tinyint NOT NULL COMMENT '科目类型（1资产 2负债 3权益 4成本 5损益）',
  `direction` tinyint NOT NULL DEFAULT 1 COMMENT '余额方向（1借方 2贷方）',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级科目ID（0=一级科目）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`, `tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='会计科目';

-- 2. 记账凭证（头）
CREATE TABLE IF NOT EXISTS biz_fms_voucher (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `voucher_no` varchar(32) NOT NULL COMMENT '凭证号（系统生成）',
  `voucher_date` date NOT NULL COMMENT '凭证日期',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0草稿 1已记账）',
  `debit_total` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '借方合计',
  `credit_total` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '贷方合计',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型（payment/return/manual）',
  `source_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`voucher_no`, `tenant_id`),
  UNIQUE KEY `uk_source` (`tenant_id`, `source_type`, `source_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='记账凭证';

-- 3. 凭证分录（明细行，借方合计 = 贷方合计）
CREATE TABLE IF NOT EXISTS biz_fms_voucher_entry (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `voucher_id` bigint NOT NULL COMMENT '凭证ID',
  `account_id` bigint NOT NULL COMMENT '科目ID',
  `account_code` varchar(32) NOT NULL COMMENT '科目编码',
  `account_name` varchar(128) NOT NULL COMMENT '科目名称',
  `summary` varchar(500) DEFAULT NULL COMMENT '分录摘要',
  `debit_amount` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '借方金额',
  `credit_amount` decimal(14,2) NOT NULL DEFAULT 0.00 COMMENT '贷方金额',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_voucher` (`voucher_id`),
  KEY `idx_account` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='凭证分录';

-- 4. 字典（幂等：按 type/value 判重）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'FMS科目类型', 'biz_fms_account_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = 'biz_fms_account_type' AND deleted = b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
SELECT t.sort, t.label, t.value, 'biz_fms_account_type', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS sort, '资产' AS label, '1' AS value UNION ALL
  SELECT 2, '负债', '2' UNION ALL
  SELECT 3, '权益', '3' UNION ALL
  SELECT 4, '成本', '4' UNION ALL
  SELECT 5, '损益', '5'
) t
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = 'biz_fms_account_type' AND deleted = b'0');

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'FMS余额方向', 'biz_fms_direction', 0, '业务字典', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = 'biz_fms_direction' AND deleted = b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
SELECT t.sort, t.label, t.value, 'biz_fms_direction', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS sort, '借方' AS label, '1' AS value UNION ALL
  SELECT 2, '贷方', '2'
) t
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = 'biz_fms_direction' AND deleted = b'0');

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'FMS凭证状态', 'biz_fms_voucher_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = 'biz_fms_voucher_status' AND deleted = b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
SELECT t.sort, t.label, t.value, 'biz_fms_voucher_status', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS sort, '草稿' AS label, '0' AS value UNION ALL
  SELECT 2, '已记账', '1'
) t
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = 'biz_fms_voucher_status' AND deleted = b'0');

-- 5. 标准会计科目种子（中小企业常用；幂等：按编码判重）
INSERT INTO biz_fms_account (`code`,`name`,`type`,`direction`,`parent_id`,`status`,`creator`,`tenant_id`)
SELECT t.code, t.name, t.type, t.direction, 0, 0, '1', 1
FROM (
  SELECT '1001' AS code, '库存现金' AS name, 1 AS type, 1 AS direction UNION ALL
  SELECT '1002', '银行存款', 1, 1 UNION ALL
  SELECT '1122', '应收账款', 1, 1 UNION ALL
  SELECT '1123', '预付账款', 1, 1 UNION ALL
  SELECT '1405', '库存商品', 1, 1 UNION ALL
  SELECT '1601', '固定资产', 1, 1 UNION ALL
  SELECT '2202', '应付账款', 2, 2 UNION ALL
  SELECT '2211', '应付职工薪酬', 2, 2 UNION ALL
  SELECT '2221', '应交税费', 2, 2 UNION ALL
  SELECT '4001', '实收资本', 3, 2 UNION ALL
  SELECT '6001', '主营业务收入', 5, 2 UNION ALL
  SELECT '6051', '其他业务收入', 5, 2 UNION ALL
  SELECT '6401', '主营业务成本', 5, 1 UNION ALL
  SELECT '6601', '销售费用', 5, 1 UNION ALL
  SELECT '6602', '管理费用', 5, 1 UNION ALL
  SELECT '6603', '财务费用', 5, 1
) t
WHERE NOT EXISTS (SELECT 1 FROM biz_fms_account a WHERE a.code = t.code AND a.tenant_id = 1 AND a.deleted = b'0');

-- 6. 菜单（企业管理 → FMS 财务目录 → 科目管理 + 凭证管理 + 按钮；幂等：按 permission 判重）
-- 6.1 目录
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT 'FMS 财务管理', 'biz:fms:query', 1, 10, m.id, 'fms', 'ep:coin', '', NULL, 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu m
WHERE m.name = '企业管理' AND m.type = 1 AND m.status = 0 AND m.deleted = b'0'
  AND NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:fms:query' AND x.deleted = b'0');
SET @fms_dir = (SELECT id FROM system_menu WHERE permission = 'biz:fms:query' AND deleted = b'0' LIMIT 1);

-- 6.2 页面菜单（parent 必须用变量取目录 id，禁止沿用上游演示菜单 1894）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '科目管理', 'biz:fms:account:query', 2, 1, @fms_dir, 'account', 'ep:collection', 'biz/fms/account/index', 'BizFmsAccount', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:fms:account:query' AND x.deleted = b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '凭证管理', 'biz:fms:voucher:query', 2, 2, @fms_dir, 'voucher', 'ep:document', 'biz/fms/voucher/index', 'BizFmsVoucher', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:fms:voucher:query' AND x.deleted = b'0');
SET @fms_account = (SELECT id FROM system_menu WHERE permission = 'biz:fms:account:query' AND deleted = b'0' LIMIT 1);
SET @fms_voucher = (SELECT id FROM system_menu WHERE permission = 'biz:fms:voucher:query' AND deleted = b'0' LIMIT 1);

-- 6.3 按钮权限
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.permission, 3, t.sort, @fms_account, '', '', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT '科目新增' AS name, 'biz:fms:account:create' AS permission, 1 AS sort UNION ALL
  SELECT '科目修改', 'biz:fms:account:update', 2 UNION ALL
  SELECT '科目删除', 'biz:fms:account:delete', 3
) t
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = t.permission AND x.deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.permission, 3, t.sort, @fms_voucher, '', '', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT '凭证新增' AS name, 'biz:fms:voucher:create' AS permission, 1 AS sort UNION ALL
  SELECT '凭证修改', 'biz:fms:voucher:update', 2 UNION ALL
  SELECT '凭证删除', 'biz:fms:voucher:delete', 3 UNION ALL
  SELECT '凭证记账', 'biz:fms:voucher:post', 4
) t
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = t.permission AND x.deleted = b'0');
