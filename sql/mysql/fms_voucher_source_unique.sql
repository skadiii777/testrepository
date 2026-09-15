-- FMS 凭证来源唯一约束（幂等）
--
-- 背景：FmsVoucherServiceImpl.createAutoPosted 采用「先查后插」实现幂等，缺少数据库级约束兜底。
--       并发场景下两个请求可同时通过 selectBySource 判空，导致同一来源单据生成两张凭证。
--       本索引把「一来源一凭证」下沉为数据库约束，并把并发冲突转化为可预期的返回。
--
-- 影响范围：仅约束 source_type / source_id 均非空的行（即自动凭证）。
--           手工凭证两列为 NULL，MySQL 唯一索引允许多个 NULL，不受影响。
--           自动凭证创建即 status=1（已记账），而 deleteVoucher 拒绝删除已记账凭证，
--           因此不存在「软删除行占用唯一键」的问题。

-- 步骤 1：重复检查（正常应返回空结果集；若有结果，需人工核对后删除多余凭证再建索引）
SELECT source_type, source_id, tenant_id, COUNT(*) AS dup_count, GROUP_CONCAT(id) AS voucher_ids
FROM biz_fms_voucher
WHERE source_type IS NOT NULL AND source_id IS NOT NULL
GROUP BY source_type, source_id, tenant_id
HAVING dup_count > 1;

-- 步骤 2：幂等建唯一索引
SET @has = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'biz_fms_voucher' AND index_name = 'uk_source');
SET @ddl = IF(@has = 0,
  'ALTER TABLE biz_fms_voucher ADD UNIQUE KEY uk_source (tenant_id, source_type, source_id)',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
