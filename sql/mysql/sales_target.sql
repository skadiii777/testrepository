-- ============================================================
-- 业绩目标（销售目标 vs 实际达成）
-- 目标按 员工(empName 冗余)+月份(yyyy-MM) 维度；实际完成从销售单汇总。
-- 幂等版：表可重复执行；菜单按 permission 判重。
-- ============================================================

-- 1. 销售目标表
CREATE TABLE IF NOT EXISTS biz_sales_target (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `emp_name` varchar(64) NOT NULL COMMENT '员工姓名（销售归属人）',
  `target_month` varchar(7) NOT NULL COMMENT '目标月份（yyyy-MM）',
  `target_amount` decimal(14,2) NOT NULL COMMENT '目标金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_month` (`emp_name`, `target_month`, `tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='销售业绩目标';

-- 2. 菜单（企业管理 → 客户合同产品目录 → 业绩目标）
SET @crm_dir = (SELECT id FROM system_menu WHERE name = '客户合同产品' AND type = 1 AND status = 0 AND deleted = b'0' LIMIT 1);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '业绩目标', 'biz:target:query', 2, 30, @crm_dir, 'target', 'ep:data-analysis', 'biz/target/index', 'BizTarget', 0, '1', NOW(), '1', NOW(), b'0'
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = 'biz:target:query' AND x.deleted = b'0');
SET @target_menu = (SELECT id FROM system_menu WHERE permission = 'biz:target:query' AND deleted = b'0' LIMIT 1);

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT t.name, t.permission, 3, t.sort, @target_menu, '', '', '', '', 0, '1', NOW(), '1', NOW(), b'0'
FROM (
  SELECT '目标新增' AS name, 'biz:target:create' AS permission, 1 AS sort UNION ALL
  SELECT '目标修改', 'biz:target:update', 2 UNION ALL
  SELECT '目标删除', 'biz:target:delete', 3
) t
WHERE NOT EXISTS (SELECT 1 FROM system_menu x WHERE x.permission = t.permission AND x.deleted = b'0');
