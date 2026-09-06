-- ----------------------------
-- 企业业务模块 SQL（enterprise-pro / yudao 架构）
-- 依赖：先导入 ruoyi-vue-pro.sql 基础库
-- ----------------------------

-- ----------------------------
-- 客户表 biz_customer
-- ----------------------------
DROP TABLE IF EXISTS biz_customer;
CREATE TABLE biz_customer (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `customer_name`  varchar(100)  DEFAULT NULL  COMMENT '客户名称',
  `contact_person`  varchar(50)  DEFAULT NULL  COMMENT '联系人',
  `phone`  varchar(30)  DEFAULT NULL  COMMENT '联系电话',
  `email`  varchar(100)  DEFAULT NULL  COMMENT '邮箱',
  `industry`  varchar(50)  DEFAULT NULL  COMMENT '所属行业',
  `source`  varchar(50)  DEFAULT NULL  COMMENT '客户来源',
  `address`  varchar(255)  DEFAULT NULL  COMMENT '地址',
  `status`  char(1)  DEFAULT NULL  COMMENT '状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='客户表';

-- ----------------------------
-- 产品表 biz_product
-- ----------------------------
DROP TABLE IF EXISTS biz_product;
CREATE TABLE biz_product (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `product_code`  varchar(50)  DEFAULT NULL  COMMENT '产品编号',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `category`  varchar(50)  DEFAULT NULL  COMMENT '产品分类',
  `unit`  varchar(20)  DEFAULT NULL  COMMENT '单位',
  `price`  decimal(12,2)  DEFAULT NULL  COMMENT '销售单价',
  `cost`  decimal(12,2)  DEFAULT NULL  COMMENT '成本价',
  `status`  char(1)  DEFAULT NULL  COMMENT '状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='产品表';

-- ----------------------------
-- 合同表 biz_contract
-- ----------------------------
DROP TABLE IF EXISTS biz_contract;
CREATE TABLE biz_contract (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `contract_code`  varchar(50)  DEFAULT NULL  COMMENT '合同编号',
  `customer_name`  varchar(100)  DEFAULT NULL  COMMENT '客户名称',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `amount`  decimal(12,2)  DEFAULT NULL  COMMENT '合同金额',
  `sign_date`  varchar(20)  DEFAULT NULL  COMMENT '签订日期',
  `start_date`  varchar(20)  DEFAULT NULL  COMMENT '开始日期',
  `end_date`  varchar(20)  DEFAULT NULL  COMMENT '结束日期',
  `owner`  varchar(50)  DEFAULT NULL  COMMENT '负责人',
  `status`  char(1)  DEFAULT NULL  COMMENT '合同状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='合同表';

-- ----------------------------
-- 供应商表 biz_supplier
-- ----------------------------
DROP TABLE IF EXISTS biz_supplier;
CREATE TABLE biz_supplier (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `supplier_name`  varchar(100)  DEFAULT NULL  COMMENT '供应商名称',
  `contact_person`  varchar(50)  DEFAULT NULL  COMMENT '联系人',
  `phone`  varchar(30)  DEFAULT NULL  COMMENT '联系电话',
  `address`  varchar(255)  DEFAULT NULL  COMMENT '地址',
  `status`  char(1)  DEFAULT NULL  COMMENT '状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商表';

-- ----------------------------
-- 采购单表 biz_purchase
-- ----------------------------
DROP TABLE IF EXISTS biz_purchase;
CREATE TABLE biz_purchase (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `purchase_code`  varchar(50)  DEFAULT NULL  COMMENT '采购单号',
  `supplier_name`  varchar(100)  DEFAULT NULL  COMMENT '供应商',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `quantity`  int  DEFAULT NULL  COMMENT '采购数量',
  `price`  decimal(12,2)  DEFAULT NULL  COMMENT '采购单价',
  `total_amount`  decimal(12,2)  DEFAULT NULL  COMMENT '总金额',
  `purchase_date`  varchar(20)  DEFAULT NULL  COMMENT '采购日期',
  `status`  char(1)  DEFAULT NULL  COMMENT '入库状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='采购单表';

-- ----------------------------
-- 销售单表 biz_sales
-- ----------------------------
DROP TABLE IF EXISTS biz_sales;
CREATE TABLE biz_sales (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `sales_code`  varchar(50)  DEFAULT NULL  COMMENT '销售单号',
  `customer_name`  varchar(100)  DEFAULT NULL  COMMENT '客户',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `quantity`  int  DEFAULT NULL  COMMENT '销售数量',
  `price`  decimal(12,2)  DEFAULT NULL  COMMENT '销售单价',
  `total_amount`  decimal(12,2)  DEFAULT NULL  COMMENT '总金额',
  `sales_date`  varchar(20)  DEFAULT NULL  COMMENT '销售日期',
  `status`  char(1)  DEFAULT NULL  COMMENT '出库状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='销售单表';

-- ----------------------------
-- 库存表 biz_stock
-- ----------------------------
DROP TABLE IF EXISTS biz_stock;
CREATE TABLE biz_stock (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `warehouse`  varchar(50)  DEFAULT NULL  COMMENT '仓库',
  `quantity`  int  DEFAULT NULL  COMMENT '库存数量',
  `min_quantity`  int  DEFAULT NULL  COMMENT '预警下限',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='库存表';

-- ----------------------------
-- 库存流水表 biz_stock_move
-- ----------------------------
DROP TABLE IF EXISTS biz_stock_move;
CREATE TABLE biz_stock_move (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `product_name`  varchar(100)  DEFAULT NULL  COMMENT '产品名称',
  `warehouse`  varchar(50)  DEFAULT NULL  COMMENT '仓库',
  `move_type`  char(1)  DEFAULT NULL  COMMENT '类型',
  `quantity`  decimal(12,2)  DEFAULT NULL  COMMENT '数量',
  `balance_after`  decimal(12,2)  DEFAULT NULL  COMMENT '结余',
  `source_type`  varchar(20)  DEFAULT NULL  COMMENT '来源类型',
  `source_code`  varchar(50)  DEFAULT NULL  COMMENT '来源单号',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='库存流水表';

-- ----------------------------
-- 员工表 biz_employee
-- ----------------------------
DROP TABLE IF EXISTS biz_employee;
CREATE TABLE biz_employee (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `emp_no`  varchar(30)  DEFAULT NULL  COMMENT '工号',
  `emp_name`  varchar(50)  DEFAULT NULL  COMMENT '姓名',
  `dept_name`  varchar(50)  DEFAULT NULL  COMMENT '部门',
  `post_name`  varchar(50)  DEFAULT NULL  COMMENT '岗位',
  `phone`  varchar(30)  DEFAULT NULL  COMMENT '联系电话',
  `email`  varchar(100)  DEFAULT NULL  COMMENT '邮箱',
  `entry_date`  varchar(20)  DEFAULT NULL  COMMENT '入职日期',
  `status`  char(1)  DEFAULT NULL  COMMENT '状态',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='员工表';

-- ----------------------------
-- 考勤表 biz_attendance
-- ----------------------------
DROP TABLE IF EXISTS biz_attendance;
CREATE TABLE biz_attendance (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `emp_name`  varchar(50)  DEFAULT NULL  COMMENT '员工姓名',
  `work_date`  varchar(20)  DEFAULT NULL  COMMENT '考勤日期',
  `check_in`  varchar(10)  DEFAULT NULL  COMMENT '上班时间',
  `check_out`  varchar(10)  DEFAULT NULL  COMMENT '下班时间',
  `status`  char(1)  DEFAULT NULL  COMMENT '考勤状态',
  `overtime_minutes`  int  DEFAULT 0  COMMENT '加班时长（分钟）',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='考勤表';

-- ----------------------------
-- 请假表 biz_leave
-- ----------------------------
DROP TABLE IF EXISTS biz_leave;
CREATE TABLE biz_leave (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `emp_name`  varchar(50)  DEFAULT NULL  COMMENT '员工姓名',
  `leave_type`  char(1)  DEFAULT NULL  COMMENT '请假类型',
  `start_date`  varchar(20)  DEFAULT NULL  COMMENT '开始日期',
  `end_date`  varchar(20)  DEFAULT NULL  COMMENT '结束日期',
  `days`  decimal(4,1)  DEFAULT NULL  COMMENT '请假天数',
  `reason`  varchar(500)  DEFAULT NULL  COMMENT '请假事由',
  `status`  char(1)  DEFAULT NULL  COMMENT '审批状态',
  `remark`  varchar(500)  DEFAULT NULL  COMMENT '审批意见',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='请假表';

-- ----------------------------
-- 假期余额表 biz_leave_quota
-- ----------------------------
DROP TABLE IF EXISTS biz_leave_quota;
CREATE TABLE biz_leave_quota (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `emp_name`  varchar(50)  DEFAULT NULL  COMMENT '员工姓名',
  `leave_type`  char(1)  DEFAULT NULL  COMMENT '假期类型',
  `year`  varchar(4)  DEFAULT NULL  COMMENT '年份',
  `quota_days`  decimal(4,1)  DEFAULT NULL  COMMENT '配额天数',
  `used_days`  decimal(4,1)  DEFAULT NULL  COMMENT '已用天数',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='假期余额表';

-- ----------------------------
-- 业务汇报表 biz_report
-- ----------------------------
DROP TABLE IF EXISTS biz_report;
CREATE TABLE biz_report (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `report_type`  char(1)  DEFAULT NULL  COMMENT '汇报类型',
  `title`  varchar(200)  DEFAULT NULL  COMMENT '标题',
  `content`  text  DEFAULT NULL  COMMENT '汇报内容',
  `report_date`  varchar(20)  DEFAULT NULL  COMMENT '汇报日期',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='业务汇报表';

-- ----------------------------
-- 费用报销表 biz_expense
-- ----------------------------
DROP TABLE IF EXISTS biz_expense;
CREATE TABLE biz_expense (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `emp_name`  varchar(50)  DEFAULT NULL  COMMENT '报销人',
  `category`  char(1)  DEFAULT NULL  COMMENT '费用类别',
  `amount`  decimal(12,2)  DEFAULT NULL  COMMENT '金额',
  `expense_date`  varchar(20)  DEFAULT NULL  COMMENT '费用发生日期',
  `reason`  varchar(500)  DEFAULT NULL  COMMENT '费用说明',
  `invoice_url`  varchar(512)  DEFAULT NULL  COMMENT '发票附件URL',
  `status`  char(1)  DEFAULT NULL  COMMENT '审批状态',
  `audit_remark`  varchar(500)  DEFAULT NULL  COMMENT '审批意见',
  `audit_by`  varchar(64)  DEFAULT NULL  COMMENT '审批人',
  `audit_time`  varchar(20)  DEFAULT NULL  COMMENT '审批时间',
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='费用报销表';

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('通用状态', 'biz_common_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '正常', '0', 'biz_common_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '停用', '1', 'biz_common_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('合同状态', 'biz_contract_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '草稿', '0', 'biz_contract_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '执行中', '1', 'biz_contract_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '已完成', '2', 'biz_contract_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '已终止', '3', 'biz_contract_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('单据状态', 'biz_order_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '草稿', '0', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '已确认', '1', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '已完成', '2', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '已作废', '3', 'biz_order_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('考勤状态', 'biz_attendance_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '正常', '0', 'biz_attendance_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '迟到', '1', 'biz_attendance_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '早退', '2', 'biz_attendance_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '缺勤', '3', 'biz_attendance_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('请假类型', 'biz_leave_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '事假', '1', 'biz_leave_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '病假', '2', 'biz_leave_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '年假', '3', 'biz_leave_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '调休', '4', 'biz_leave_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('请假审批状态', 'biz_leave_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '待审批', '0', 'biz_leave_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '已通过', '1', 'biz_leave_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '已驳回', '2', 'biz_leave_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '已销假', '3', 'biz_leave_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('汇报类型', 'biz_report_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '日报', '1', 'biz_report_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '周报', '2', 'biz_report_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '月报', '3', 'biz_report_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('报销类别', 'biz_expense_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '差旅', '1', 'biz_expense_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '餐费', '2', 'biz_expense_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '办公', '3', 'biz_expense_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (4, '其他', '4', 'biz_expense_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('报销审批状态', 'biz_expense_status', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '待审批', '0', 'biz_expense_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '已通过', '1', 'biz_expense_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (3, '已驳回', '2', 'biz_expense_status', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES ('库存流水类型', 'biz_stock_move_type', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (1, '入库', '1', 'biz_stock_move_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) VALUES (2, '出库', '2', 'biz_stock_move_type', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('企业管理', '', 1, 10, 0, '/biz', 'suitcase', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dirBiz = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工工作台', '', 1, 5, 0, '/portal', 'user', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dirPortal = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户合同产品', '', 1, 1, @dirBiz, '/客户合同产品', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dir = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户', 'biz:customer:query', 2, 1, @dir, 'customer', '#', 'biz/customer/index', 'Customer', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户查询', 'biz:customer:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户新增', 'biz:customer:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户修改', 'biz:customer:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户删除', 'biz:customer:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('客户导出', 'biz:customer:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品', 'biz:product:query', 2, 1, @dir, 'product', '#', 'biz/product/index', 'Product', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品查询', 'biz:product:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品新增', 'biz:product:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品修改', 'biz:product:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品删除', 'biz:product:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('产品导出', 'biz:product:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同', 'biz:contract:query', 2, 1, @dir, 'contract', '#', 'biz/contract/index', 'Contract', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同查询', 'biz:contract:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同新增', 'biz:contract:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同修改', 'biz:contract:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同删除', 'biz:contract:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('合同导出', 'biz:contract:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商', 'biz:supplier:query', 2, 1, @dir, 'supplier', '#', 'biz/supplier/index', 'Supplier', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商查询', 'biz:supplier:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商新增', 'biz:supplier:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商修改', 'biz:supplier:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商删除', 'biz:supplier:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('供应商导出', 'biz:supplier:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('进销存管理', '', 1, 1, @dirBiz, '/进销存管理', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dir = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单', 'biz:purchase:query', 2, 1, @dir, 'purchase', '#', 'biz/purchase/index', 'Purchase', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单查询', 'biz:purchase:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单新增', 'biz:purchase:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单修改', 'biz:purchase:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单删除', 'biz:purchase:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('采购单导出', 'biz:purchase:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单', 'biz:sales:query', 2, 1, @dir, 'sales', '#', 'biz/sales/index', 'Sales', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单查询', 'biz:sales:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单新增', 'biz:sales:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单修改', 'biz:sales:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单删除', 'biz:sales:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('销售单导出', 'biz:sales:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存', 'biz:stock:query', 2, 1, @dir, 'stock', '#', 'biz/stock/index', 'Stock', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存查询', 'biz:stock:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存新增', 'biz:stock:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存修改', 'biz:stock:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存删除', 'biz:stock:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存导出', 'biz:stock:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水', 'biz:stockmove:query', 2, 1, @dir, 'stockmove', '#', 'biz/stockmove/index', 'Stockmove', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水查询', 'biz:stockmove:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水新增', 'biz:stockmove:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水修改', 'biz:stockmove:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水删除', 'biz:stockmove:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('库存流水导出', 'biz:stockmove:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('人事考勤', '', 1, 1, @dirBiz, '/人事考勤', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dir = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工', 'biz:employee:query', 2, 1, @dir, 'employee', '#', 'biz/employee/index', 'Employee', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工查询', 'biz:employee:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工新增', 'biz:employee:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工修改', 'biz:employee:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工删除', 'biz:employee:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('员工导出', 'biz:employee:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤', 'biz:attendance:query', 2, 1, @dir, 'attendance', '#', 'biz/attendance/index', 'Attendance', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤查询', 'biz:attendance:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤新增', 'biz:attendance:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤修改', 'biz:attendance:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤删除', 'biz:attendance:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('考勤导出', 'biz:attendance:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假', 'biz:leave:query', 2, 1, @dir, 'leave', '#', 'biz/leave/index', 'Leave', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假查询', 'biz:leave:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假新增', 'biz:leave:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假修改', 'biz:leave:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假删除', 'biz:leave:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假导出', 'biz:leave:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假审批', 'biz:leave:audit', 3, 0, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额', 'biz:quota:query', 2, 1, @dir, 'quota', '#', 'biz/quota/index', 'Quota', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额查询', 'biz:quota:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额新增', 'biz:quota:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额修改', 'biz:quota:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额删除', 'biz:quota:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('假期余额导出', 'biz:quota:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('协作审批', '', 1, 1, @dirBiz, '/协作审批', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
SET @dir = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报', 'biz:report:query', 2, 1, @dir, 'report', '#', 'biz/report/index', 'Report', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报查询', 'biz:report:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报新增', 'biz:report:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报修改', 'biz:report:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报删除', 'biz:report:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报导出', 'biz:report:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销', 'biz:expense:query', 2, 1, @dir, 'expense', '#', 'biz/expense/index', 'Expense', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销查询', 'biz:expense:query', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销新增', 'biz:expense:create', 3, 2, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销修改', 'biz:expense:update', 3, 3, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销删除', 'biz:expense:delete', 3, 4, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销导出', 'biz:expense:export', 3, 5, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('费用报销审批', 'biz:expense:audit', 3, 0, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('审批中心', 'biz:approval:query', 2, 2, @dirBiz, 'approval', '#', 'biz/approval/index', 'Approval', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('数据看板', 'biz:dashboard:query', 2, 0, @dirBiz, 'dashboard', '#', 'biz/dashboard/index', 'Dashboard', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('我的打卡', 'portal:index:query', 2, 1, @dirPortal, 'index', '#', 'portal/index', 'PortalIndex', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('打卡操作', 'portal:punch:add', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('我的请假', 'portal:leave:query', 2, 2, @dirPortal, 'leave', '#', 'portal/leave/index', 'PortalLeave', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('请假提交', 'portal:leave:add', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('业务汇报', 'portal:report:query', 2, 3, @dirPortal, 'report', '#', 'portal/report/index', 'PortalReport', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('汇报提交', 'portal:report:add', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('我的报销', 'portal:expense:query', 2, 4, @dirPortal, 'expense', '#', 'portal/expense/index', 'PortalExpense', 0, '1', NOW(), '1', NOW(), b'0');
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted) VALUES ('报销提交', 'portal:expense:add', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) SELECT 2, id, '1', NOW(), '1', NOW(), b'0' FROM system_menu WHERE permission LIKE 'portal:%';