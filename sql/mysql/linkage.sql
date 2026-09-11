-- ============================================================
-- 模块联动增强（赢单转合同 / 合同回款 / 退货红冲）
-- 幂等性说明：ALTER 重复执行会报 Duplicate column，属预期
-- ============================================================

-- 1) 收付款可选挂合同（合同回款进度）
ALTER TABLE `biz_payment`
  ADD COLUMN `contract_id` bigint NULL DEFAULT NULL COMMENT '关联合同 id（收款可选挂合同）' AFTER `payment_method`;

-- 2) 合同备注（商机转合同时记录转化来源）
ALTER TABLE `biz_contract`
  ADD COLUMN `remark` varchar(500) NULL DEFAULT NULL COMMENT '备注' AFTER `status`;

-- 3) 回款进度查询索引
ALTER TABLE `biz_payment` ADD INDEX `idx_contract` (`contract_id`);

-- 4) 纯合同回款（不挂单据）时 order_id/order_code 允许为空
ALTER TABLE `biz_payment` MODIFY COLUMN `order_id` bigint NULL DEFAULT NULL COMMENT '关联单据 id（纯合同回款时为空）';
ALTER TABLE `biz_payment` MODIFY COLUMN `order_code` varchar(64) NULL DEFAULT NULL COMMENT '关联单据编号';

-- ============================================================
-- 停用组件缺失的 404 菜单（上游演示残留：三方登录管理页×2、代码生成案例）
-- ============================================================
UPDATE system_menu SET `status` = 1 WHERE `id` IN (530, 535, 83);

-- 5) 停用 Java 监控菜单（需独立 Spring Boot Admin 服务，未部署）
UPDATE system_menu SET `status` = 1 WHERE `path` LIKE '%admin-server%' AND `name` LIKE '%监控%';

-- 6) 停用上游演示 Job（对应模块已删，handler 不存在会空跑刷错误日志）
UPDATE infra_job SET `status` = 2 WHERE `name` LIKE '支付%' OR `name` LIKE 'Mall %' OR `name` LIKE 'IoT %'
   OR `name` LIKE 'CMS %' OR `name` LIKE 'HRM %' OR `name` LIKE 'PMS %' OR `name` IN ('demoJob', '转账订单的同步 Job');
