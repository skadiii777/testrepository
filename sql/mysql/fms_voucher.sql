-- ============================================================
-- FMS 财务管理 Batch 1：会计科目 + 记账凭证 + 分录
-- 标准借贷记账法：凭证分录借方合计 = 贷方合计
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
  UNIQUE KEY `uk_no` (`voucher_no`, `tenant_id`)
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

-- 4. 标准会计科目种子（中小企业常用）
INSERT INTO biz_fms_account (`code`,`name`,`type`,`direction`,`parent_id`,`status`,`creator`,`tenant_id`) VALUES
('1001','库存现金',1,1,0,0,'1',1),
('1002','银行存款',1,1,0,0,'1',1),
('1122','应收账款',1,1,0,0,'1',1),
('1123','预付账款',1,1,0,0,'1',1),
('1405','库存商品',1,1,0,0,'1',1),
('1601','固定资产',1,1,0,0,'1',1),
('2202','应付账款',2,2,0,0,'1',1),
('2211','应付职工薪酬',2,2,0,0,'1',1),
('2221','应交税费',2,2,0,0,'1',1),
('4001','实收资本',3,2,0,0,'1',1),
('6001','主营业务收入',5,2,0,0,'1',1),
('6051','其他业务收入',5,2,0,0,'1',1),
('6401','主营业务成本',5,1,0,0,'1',1),
('6601','销售费用',5,1,0,0,'1',1),
('6602','管理费用',5,1,0,0,'1',1),
('6603','财务费用',5,1,0,0,'1',1);

-- 5. 菜单（企业管理 → FMS 财务目录 → 科目管理 + 凭证管理）
INSERT INTO system_menu (name,permission,type,sort,parent_id,path,icon,component,component_name,status,creator,create_time,updater,update_time,deleted)
SELECT 'FMS 财务管理','biz:fms:query',1,10,id,'fms','ep:coin','',NULL,0,'1',NOW(),'1',NOW(),b'0'
FROM system_menu WHERE name='企业管理' AND type=1 LIMIT 1;
SET @fms = LAST_INSERT_ID();
INSERT INTO system_menu (name,permission,type,sort,parent_id,path,icon,componentcomponent_name,status,creator,create_time,updater,update_time,deleted) VALUES
('科目管理','biz:fms:account:query',2,1,@fms,'account','ep:collection','biz/fms/account/index','BizFmsAccount',0,'1',NOW(),'1',NOW(),b'0'),
('凭证管理','biz:fms:voucher:query',2,2,@fms,'voucher','ep:document','biz/fms/voucher/index','BizFmsVoucher',0,'1',NOW(),'1',NOW(),b'0',1);
SET @fms_sub = LAST_INSERT_ID() - 1;
INSERT INTO system_menu (name,permission,type,sort,parent_id,path,icon,component,component_name,status,creator,create_time,updater,update_time,deleted,) VALUES
('科目新增','biz:fms:account:create',3,1,@fms_sub,'','','','',0,'1',NOW(),'1',NOW(),b'0',1),
('科目修改','biz:fms:account:update',3,2,@fms_sub,'','','','',0,'1',NOW(),'1',NOW(),b'0',1),
('科目删除','biz:fms:account:delete',3,3,@fms_sub,'','','','',0,'1',NOW(),'1',NOW(),b'0',1);
INSERT INTO system_menu (name,permission,type,sort,parent_id,path,icon,component,component_name,status,creator,create_time,updater,update_time,deleted,) VALUES
('凭证新增','biz:fms:voucher:create',3,1,@fms_sub+1,'','','','',0,'1',NOW(),'1',NOW(),b'0',1),
('凭证修改','biz:fms:voucher:update',3,2,@fms_sub+1,'','','','',0,'1',NOW(),'1',NOW(),b'0',1),
('凭证删除','biz:fms:voucher:delete',3,3,@fms_sub+1,'','','','',0,'1',NOW(),'1',NOW(),b'0',1),
('凭证记账','biz:fms:voucher:post',3,4,@fms_sub+1,'','','','',0,'1',NOW(),'1',NOW(),b'0',1);