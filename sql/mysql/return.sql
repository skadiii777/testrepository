-- ============================================================
-- 退货管理（对齐 yudao ERP ErpSaleReturn / ErpPurchaseReturn 概念）
-- 销售单（已完成）发起销售退货：执行后货物入库；
-- 采购单（已完成）发起采购退货：执行后货物出库（退回供应商）；
-- 同一原单累计退货数量（不含已作废）不可超过原单数量。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 表
CREATE TABLE biz_return (
  `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `return_no`    varchar(32)   NOT NULL                COMMENT '退货单号（SR/PR+时间戳）',
  `return_type`  char(1)       NOT NULL                COMMENT '退货类型（1=销售退货 2=采购退货）',
  `order_id`     bigint        NOT NULL                COMMENT '关联单据 id',
  `order_code`   varchar(64)   NOT NULL                COMMENT '关联单据编号',
  `party_name`   varchar(128)  DEFAULT NULL            COMMENT '对方名称（客户/供应商）',
  `product_name` varchar(128)  NOT NULL                COMMENT '产品名称',
  `warehouse`    varchar(64)   NOT NULL DEFAULT '默认仓库' COMMENT '仓库',
  `quantity`     bigint        NOT NULL                COMMENT '退货数量',
  `price`        decimal(12,2) NOT NULL                COMMENT '退货单价',
  `total_amount` decimal(12,2) NOT NULL                COMMENT '总金额',
  `return_date`  varchar(20)   NOT NULL                COMMENT '退货日期',
  `reason`       varchar(255)  NOT NULL                COMMENT '退货原因',
  `status`       char(1)       NOT NULL DEFAULT '0'    COMMENT '状态（0=待退货 1=已退货 3=已作废）',
  `remark`       varchar(500)  DEFAULT NULL            COMMENT '备注',
  `creator`      varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`      varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      bit(1)        NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`    bigint        NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`return_type`, `order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='退货单表';

-- 2. 字典（system_dict_data 为全局表，无 tenant_id 列）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('退货类型', 'biz_return_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '销售退货', '1', 'biz_return_type', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '采购退货', '2', 'biz_return_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('退货状态', 'biz_return_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '待退货', '0', 'biz_return_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '已退货', '1', 'biz_return_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '已作废', '3', 'biz_return_status', 0, '1', NOW(), '1', NOW(), b'0');

-- 3. 管理端菜单：企业管理 → 退货管理（system_menu 为全局表；超管免授权自动可见，
--    普通角色按需在 role_menu 里绑定 biz:return:* 行，行本身需 tenant_id=1）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '退货管理', 'biz:return:query', 2, 7, id, 'return', 'fa:undo', 'biz/return/index', 'BizReturn', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '企业管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('退货新增', 'biz:return:create', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('退货更新', 'biz:return:update', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('退货删除', 'biz:return:delete', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
