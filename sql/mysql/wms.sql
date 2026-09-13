-- ============================================================
-- WMS 仓储管理 初步搭建（W1）：库位主数据 + 库位库存 + 库位流水
-- 定位：库位库存 = 仓库库存的分配视图（上架/下架/移库只动库位库存，
--       不动主库存 biz_stock）；未分配量 = 仓库库存 - 库位合计。
-- 幂等版：表/字典/菜单均可重复执行。
-- 执行前提：enterprise-biz.sql 已导入（system_menu 含「进销存管理」目录）。
-- ============================================================

-- 1. 库位主数据（挂现有仓库主数据 warehouse_id）
CREATE TABLE IF NOT EXISTS biz_wms_location (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID（biz_warehouse）',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称（冗余）',
  `code` varchar(64) NOT NULL COMMENT '库位编码（如 A-01-01）',
  `name` varchar(128) DEFAULT NULL COMMENT '库位名称',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '库位类型（1存储区 2拣货区 3收货区 4退货区）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0启用 1停用）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wh_code` (`warehouse_id`, `code`, `tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='WMS库位';

-- 2. 库位库存（产品在库位上的分配量）
CREATE TABLE IF NOT EXISTS biz_wms_location_stock (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称（冗余）',
  `location_id` bigint NOT NULL COMMENT '库位ID',
  `location_code` varchar(64) NOT NULL COMMENT '库位编码（冗余）',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_name` varchar(128) NOT NULL COMMENT '产品名称（冗余）',
  `quantity` bigint NOT NULL DEFAULT 0 COMMENT '库位数量（>=0）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_loc_product` (`location_id`, `product_id`, `tenant_id`),
  KEY `idx_wh_product` (`warehouse_id`, `product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='WMS库位库存';

-- 3. 库位流水（只增不改）
CREATE TABLE IF NOT EXISTS biz_wms_location_move (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `move_type` varchar(16) NOT NULL COMMENT '动作类型（putaway上架 remove下架 move移库）',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_name` varchar(128) NOT NULL COMMENT '产品名称（冗余）',
  `quantity` bigint NOT NULL COMMENT '数量（正数）',
  `from_location_id` bigint DEFAULT NULL COMMENT '源库位ID（上架时为空=未分配区）',
  `from_location_code` varchar(64) DEFAULT NULL COMMENT '源库位编码',
  `to_location_id` bigint DEFAULT NULL COMMENT '目标库位ID（下架时为空=未分配区）',
  `to_location_code` varchar(64) DEFAULT NULL COMMENT '目标库位编码',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_wh_product` (`warehouse_id`, `product_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='WMS库位流水';

-- 4. 字典（幂等）
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'WMS库位类型', 'biz_wms_location_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = 'biz_wms_location_type' AND deleted = b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
SELECT t.sort, t.label, t.value, 'biz_wms_location_type', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS sort, '存储区' AS label, '1' AS value UNION ALL
  SELECT 2, '拣货区', '2' UNION ALL
  SELECT 3, '收货区', '3' UNION ALL
  SELECT 4, '退货区', '4'
) t
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = 'biz_wms_location_type' AND deleted = b'0');

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
SELECT 'WMS库位动作', 'biz_wms_move_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_dict_type WHERE type = 'biz_wms_move_type' AND deleted = b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
SELECT t.sort, t.label, t.value, 'biz_wms_move_type', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT 1 AS sort, '上架' AS label, 'putaway' AS value UNION ALL
  SELECT 2, '下架', 'remove' UNION ALL
  SELECT 3, '移库', 'move'
) t
WHERE NOT EXISTS (SELECT 1 FROM system_dict_data WHERE dict_type = 'biz_wms_move_type' AND deleted = b'0');

-- 5. 菜单（进销存管理 → WMS 库位管理 + WMS 库位库存；幂等：按 permission 判重）
SET @invt = (SELECT id FROM system_menu WHERE name = '进销存管理' AND type = 1 AND status = 0 AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT 'WMS 库位管理', 'biz:wms:query', 2, 20, @invt, 'wms-location', 'ep:grid', 'biz/wms/location/index', 'BizWmsLocation', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:wms:query' AND x.deleted = b'0');
SET @wms_loc = (SELECT id FROM system_menu WHERE permission = 'biz:wms:query' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT 'WMS 库位库存', 'biz:wms:stock:query', 2, 21, @invt, 'wms-stock', 'ep:box', 'biz/wms/stock/index', 'BizWmsStock', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:wms:stock:query' AND x.deleted = b'0');
SET @wms_stock = (SELECT id FROM system_menu WHERE permission = 'biz:wms:stock:query' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.permission, 3, t.sort, @wms_loc, '', '', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT '库位新增' AS name, 'biz:wms:create' AS permission, 1 AS sort UNION ALL
  SELECT '库位修改', 'biz:wms:update', 2 UNION ALL
  SELECT '库位删除', 'biz:wms:delete', 3
) t
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = t.permission AND x.deleted = b'0');

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '库位动作', 'biz:wms:stock:operate', 3, 1, @wms_stock, '', '', '', '', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:wms:stock:operate' AND x.deleted = b'0');
