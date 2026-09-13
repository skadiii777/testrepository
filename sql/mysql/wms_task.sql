-- ============================================================
-- WMS W2：单据驱动的库位作业任务
-- 上架任务：采购单完成入库 → 自动生成，库位上架动作按 FIFO 消耗
-- 拣货任务：销售单完成出库 → 自动生成（此时主库存已扣、货仍在库位，
--           未分配量变为负数即"待拣货"），下架动作按 FIFO 消耗
-- 幂等版：可重复执行。
-- ============================================================

-- 1. 上架任务（采购入库维度）
CREATE TABLE IF NOT EXISTS biz_wms_putaway_task (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `purchase_id` bigint NOT NULL COMMENT '采购单ID',
  `purchase_code` varchar(64) NOT NULL COMMENT '采购单号',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_name` varchar(128) NOT NULL COMMENT '产品名称（冗余）',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称（冗余）',
  `quantity` bigint NOT NULL COMMENT '入库数量',
  `putaway_quantity` bigint NOT NULL DEFAULT 0 COMMENT '已上架累计',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待上架 1已完成）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase` (`purchase_id`, `tenant_id`),
  KEY `idx_status_wh` (`status`, `warehouse_id`, `product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='WMS上架任务';

-- 2. 拣货任务（销售出库维度）
CREATE TABLE IF NOT EXISTS biz_wms_pick_task (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sales_id` bigint NOT NULL COMMENT '销售单ID',
  `sales_code` varchar(64) NOT NULL COMMENT '销售单号',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `product_name` varchar(128) NOT NULL COMMENT '产品名称（冗余）',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `warehouse_name` varchar(128) NOT NULL COMMENT '仓库名称（冗余）',
  `quantity` bigint NOT NULL COMMENT '出库数量',
  `picked_quantity` bigint NOT NULL DEFAULT 0 COMMENT '已拣货累计',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待拣货 1已完成）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales` (`sales_id`, `tenant_id`),
  KEY `idx_status_wh` (`status`, `warehouse_id`, `product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='WMS拣货任务';
