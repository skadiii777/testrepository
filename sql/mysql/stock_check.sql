-- ============================================================
-- 库存盘点（对齐 yudao ERP StockCheck 库存盘点）
-- 创建盘点单快照账面数量；确认时按实盘调整库存并写库存流水
-- （sourceType=stockcheck，盘盈入库/盘亏出库沿用量值语义）。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 表
CREATE TABLE biz_stock_check (
  `id`              bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `check_no`        varchar(32)  NOT NULL                COMMENT '盘点单号（PD+时间戳）',
  `product_name`    varchar(128) NOT NULL                COMMENT '产品名称',
  `warehouse`       varchar(64)  NOT NULL DEFAULT '默认仓库' COMMENT '仓库',
  `book_quantity`   bigint       DEFAULT NULL            COMMENT '账面数量（确认时快照）',
  `actual_quantity` bigint       NOT NULL                COMMENT '实盘数量',
  `diff_quantity`   bigint       DEFAULT NULL            COMMENT '差异（实盘-账面）',
  `status`          char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0=待确认 1=已确认）',
  `check_date`      varchar(20)  NOT NULL                COMMENT '盘点日期',
  `remark`          varchar(500) DEFAULT NULL            COMMENT '备注',
  `creator`         varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`         varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         bit(1)       NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`       bigint       NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='库存盘点单表';

-- 2. 字典
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('盘点状态', 'biz_check_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '待确认', '0', 'biz_check_status', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '已确认', '1', 'biz_check_status', 0, '1', NOW(), '1', NOW(), b'0');

-- 3. 管理端菜单：企业管理 → 库存盘点（超管免授权自动可见）
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '库存盘点', 'biz:stockcheck:query', 2, 7, id, 'stockcheck', 'ep:box', 'biz/stockcheck/index', 'BizStockCheck', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '企业管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('盘点确认', 'biz:stockcheck:confirm', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('盘点删除', 'biz:stockcheck:delete', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
