-- ============================================================
-- 销售归属人（业绩目标联动前置）：biz_sales 加 emp_name
-- 创建销售单时自动填充当前登录人昵称，可修改。
-- 幂等版：仅当列不存在时执行。
-- ============================================================
SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'biz_sales' AND column_name = 'emp_name');
SET @ddl = IF(@has_col = 0,
  'ALTER TABLE biz_sales ADD COLUMN emp_name varchar(64) DEFAULT NULL COMMENT ''销售归属人（登录人昵称，可修改）'' AFTER customer_name',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 索引（目标汇总按 归属人+日期范围 查已完成销售单）
SET @has_idx = (SELECT COUNT(*) FROM information_schema.statistics
  WHERE table_schema = DATABASE() AND table_name = 'biz_sales' AND index_name = 'idx_emp_status');
SET @ddl2 = IF(@has_idx = 0,
  'ALTER TABLE biz_sales ADD INDEX idx_emp_status (emp_name, status)',
  'SELECT 1');
PREPARE stmt FROM @ddl2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
