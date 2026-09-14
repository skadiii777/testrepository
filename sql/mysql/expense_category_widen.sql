-- 报销类别放宽：char(1) 存不下中文类别名（历史遗留，幂等）
SET @has = (SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_expense' AND column_name = 'category');
SET @ddl = IF(@has = 1,
  'ALTER TABLE biz_expense MODIFY COLUMN category varchar(32) DEFAULT NULL COMMENT ''报销类别（差旅费/办公费等）''',
  'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
