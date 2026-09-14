-- 客户信用额度（幂等）：biz_customer 加 credit_limit（0=不限额）
SET @has = (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_customer' AND column_name = 'credit_limit');
SET @ddl = IF(@has = 0,
  'ALTER TABLE biz_customer ADD COLUMN credit_limit decimal(14,2) NOT NULL DEFAULT 0 COMMENT ''信用额度（0=不限额）'' AFTER address',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
