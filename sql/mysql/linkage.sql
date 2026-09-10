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
