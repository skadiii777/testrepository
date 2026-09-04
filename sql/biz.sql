-- ----------------------------
-- 企业管理系统业务表（客户/合同/产品、进销存、员工/考勤/请假）
-- 在 ry_20260319.sql 导入后执行
-- ----------------------------
-- ----------------------------
-- 客户表 biz_customer
-- ----------------------------
drop table if exists biz_customer;
create table biz_customer (
  customer_id       bigint(20)      not null auto_increment    comment '主键ID',
  customer_name varchar(100) COMMENT '客户名称',
    contact_person varchar(50) COMMENT '联系人',
    phone varchar(30) COMMENT '联系电话',
    email varchar(100) COMMENT '邮箱',
    industry varchar(50) COMMENT '所属行业',
    source varchar(50) COMMENT '客户来源',
    address varchar(255) COMMENT '地址',
    status char(1) COMMENT '状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (customer_id)
) engine=innodb auto_increment=1 comment = '客户表';

-- ----------------------------
-- 产品表 biz_product
-- ----------------------------
drop table if exists biz_product;
create table biz_product (
  product_id       bigint(20)      not null auto_increment    comment '主键ID',
  product_code varchar(50) COMMENT '产品编号',
    product_name varchar(100) COMMENT '产品名称',
    category varchar(50) COMMENT '产品分类',
    unit varchar(20) COMMENT '单位',
    price decimal(12,2) COMMENT '销售单价',
    cost decimal(12,2) COMMENT '成本价',
    status char(1) COMMENT '状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (product_id)
) engine=innodb auto_increment=1 comment = '产品表';

-- ----------------------------
-- 合同表 biz_contract
-- ----------------------------
drop table if exists biz_contract;
create table biz_contract (
  contract_id       bigint(20)      not null auto_increment    comment '主键ID',
  contract_code varchar(50) COMMENT '合同编号',
    customer_name varchar(100) COMMENT '客户名称',
    product_name varchar(100) COMMENT '产品名称',
    amount decimal(12,2) COMMENT '合同金额',
    sign_date varchar(20) COMMENT '签订日期',
    start_date varchar(20) COMMENT '开始日期',
    end_date varchar(20) COMMENT '结束日期',
    owner varchar(50) COMMENT '负责人',
    status char(1) COMMENT '合同状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (contract_id)
) engine=innodb auto_increment=1 comment = '合同表';

-- ----------------------------
-- 供应商表 biz_supplier
-- ----------------------------
drop table if exists biz_supplier;
create table biz_supplier (
  supplier_id       bigint(20)      not null auto_increment    comment '主键ID',
  supplier_name varchar(100) COMMENT '供应商名称',
    contact_person varchar(50) COMMENT '联系人',
    phone varchar(30) COMMENT '联系电话',
    address varchar(255) COMMENT '地址',
    status char(1) COMMENT '状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (supplier_id)
) engine=innodb auto_increment=1 comment = '供应商表';

-- ----------------------------
-- 采购单表 biz_purchase
-- ----------------------------
drop table if exists biz_purchase;
create table biz_purchase (
  purchase_id       bigint(20)      not null auto_increment    comment '主键ID',
  purchase_code varchar(50) COMMENT '采购单号',
    supplier_name varchar(100) COMMENT '供应商',
    product_name varchar(100) COMMENT '产品名称',
    quantity int(11) COMMENT '采购数量',
    price decimal(12,2) COMMENT '采购单价',
    total_amount decimal(12,2) COMMENT '总金额',
    purchase_date varchar(20) COMMENT '采购日期',
    status char(1) COMMENT '入库状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (purchase_id)
) engine=innodb auto_increment=1 comment = '采购单表';

-- ----------------------------
-- 销售单表 biz_sales
-- ----------------------------
drop table if exists biz_sales;
create table biz_sales (
  sales_id       bigint(20)      not null auto_increment    comment '主键ID',
  sales_code varchar(50) COMMENT '销售单号',
    customer_name varchar(100) COMMENT '客户',
    product_name varchar(100) COMMENT '产品名称',
    quantity int(11) COMMENT '销售数量',
    price decimal(12,2) COMMENT '销售单价',
    total_amount decimal(12,2) COMMENT '总金额',
    sales_date varchar(20) COMMENT '销售日期',
    status char(1) COMMENT '出库状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (sales_id)
) engine=innodb auto_increment=1 comment = '销售单表';

-- ----------------------------
-- 库存表 biz_stock
-- ----------------------------
drop table if exists biz_stock;
create table biz_stock (
  stock_id       bigint(20)      not null auto_increment    comment '主键ID',
  product_name varchar(100) COMMENT '产品名称',
    warehouse varchar(50) COMMENT '仓库',
    quantity int(11) COMMENT '库存数量',
    min_quantity int(11) COMMENT '预警下限',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (stock_id)
) engine=innodb auto_increment=1 comment = '库存表';

-- ----------------------------
-- 员工表 biz_employee
-- ----------------------------
drop table if exists biz_employee;
create table biz_employee (
  employee_id       bigint(20)      not null auto_increment    comment '主键ID',
  emp_no varchar(30) COMMENT '工号',
    emp_name varchar(50) COMMENT '姓名',
    dept_name varchar(50) COMMENT '部门',
    post_name varchar(50) COMMENT '岗位',
    phone varchar(30) COMMENT '联系电话',
    email varchar(100) COMMENT '邮箱',
    entry_date varchar(20) COMMENT '入职日期',
    status char(1) COMMENT '状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (employee_id)
) engine=innodb auto_increment=1 comment = '员工表';

-- ----------------------------
-- 考勤表 biz_attendance
-- ----------------------------
drop table if exists biz_attendance;
create table biz_attendance (
  attendance_id       bigint(20)      not null auto_increment    comment '主键ID',
  emp_name varchar(50) COMMENT '员工姓名',
    work_date varchar(20) COMMENT '考勤日期',
    check_in varchar(10) COMMENT '上班时间',
    check_out varchar(10) COMMENT '下班时间',
    status char(1) COMMENT '考勤状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (attendance_id)
) engine=innodb auto_increment=1 comment = '考勤表';

-- ----------------------------
-- 请假表 biz_leave
-- ----------------------------
drop table if exists biz_leave;
create table biz_leave (
  leave_id       bigint(20)      not null auto_increment    comment '主键ID',
  emp_name varchar(50) COMMENT '员工姓名',
    leave_type char(1) COMMENT '请假类型',
    start_date varchar(20) COMMENT '开始日期',
    end_date varchar(20) COMMENT '结束日期',
    days decimal(4,1) COMMENT '请假天数',
    reason varchar(500) COMMENT '请假事由',
    status char(1) COMMENT '审批状态',
  create_by      varchar(64)     default ''                 comment '创建者',
  create_time    datetime                                   comment '创建时间',
  update_by      varchar(64)     default ''                 comment '更新者',
  update_time    datetime                                   comment '更新时间',
  remark         varchar(500)    default null               comment '备注',
  primary key (leave_id)
) engine=innodb auto_increment=1 comment = '请假表';

-- ----------------------------
-- 菜单
-- ----------------------------
insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('企业管理', '0', '5', '#', 'M', '0', '', 'fa fa-briefcase', 'admin', sysdate());
set @parentId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('客户合同产品', @parentId, '1', '#', 'M', '0', '', 'fa fa-handshake-o', 'admin', sysdate());
set @dirCrm = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('进销存管理', @parentId, '2', '#', 'M', '0', '', 'fa fa-truck', 'admin', sysdate());
set @dirErp = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('人事考勤', @parentId, '3', '#', 'M', '0', '', 'fa fa-users', 'admin', sysdate());
set @dirHr = @@identity;

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('客户', @dirCrm, '1', 'biz/customer', 'C', '0', 'biz:customer:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('客户查询', @menuId, '1', 'F', '0', 'biz:customer:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('客户新增', @menuId, '2', 'F', '0', 'biz:customer:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('客户修改', @menuId, '3', 'F', '0', 'biz:customer:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('客户删除', @menuId, '4', 'F', '0', 'biz:customer:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('产品', @dirCrm, '1', 'biz/product', 'C', '0', 'biz:product:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('产品查询', @menuId, '1', 'F', '0', 'biz:product:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('产品新增', @menuId, '2', 'F', '0', 'biz:product:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('产品修改', @menuId, '3', 'F', '0', 'biz:product:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('产品删除', @menuId, '4', 'F', '0', 'biz:product:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('合同', @dirCrm, '1', 'biz/contract', 'C', '0', 'biz:contract:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('合同查询', @menuId, '1', 'F', '0', 'biz:contract:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('合同新增', @menuId, '2', 'F', '0', 'biz:contract:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('合同修改', @menuId, '3', 'F', '0', 'biz:contract:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('合同删除', @menuId, '4', 'F', '0', 'biz:contract:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('供应商', @dirErp, '1', 'biz/supplier', 'C', '0', 'biz:supplier:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('供应商查询', @menuId, '1', 'F', '0', 'biz:supplier:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('供应商新增', @menuId, '2', 'F', '0', 'biz:supplier:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('供应商修改', @menuId, '3', 'F', '0', 'biz:supplier:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('供应商删除', @menuId, '4', 'F', '0', 'biz:supplier:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('采购单', @dirErp, '1', 'biz/purchase', 'C', '0', 'biz:purchase:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('采购单查询', @menuId, '1', 'F', '0', 'biz:purchase:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('采购单新增', @menuId, '2', 'F', '0', 'biz:purchase:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('采购单修改', @menuId, '3', 'F', '0', 'biz:purchase:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('采购单删除', @menuId, '4', 'F', '0', 'biz:purchase:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('销售单', @dirErp, '1', 'biz/sales', 'C', '0', 'biz:sales:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('销售单查询', @menuId, '1', 'F', '0', 'biz:sales:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('销售单新增', @menuId, '2', 'F', '0', 'biz:sales:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('销售单修改', @menuId, '3', 'F', '0', 'biz:sales:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('销售单删除', @menuId, '4', 'F', '0', 'biz:sales:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('库存', @dirErp, '1', 'biz/stock', 'C', '0', 'biz:stock:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('库存查询', @menuId, '1', 'F', '0', 'biz:stock:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('库存新增', @menuId, '2', 'F', '0', 'biz:stock:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('库存修改', @menuId, '3', 'F', '0', 'biz:stock:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('库存删除', @menuId, '4', 'F', '0', 'biz:stock:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('员工', @dirHr, '1', 'biz/employee', 'C', '0', 'biz:employee:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('员工查询', @menuId, '1', 'F', '0', 'biz:employee:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('员工新增', @menuId, '2', 'F', '0', 'biz:employee:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('员工修改', @menuId, '3', 'F', '0', 'biz:employee:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('员工删除', @menuId, '4', 'F', '0', 'biz:employee:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('考勤', @dirHr, '1', 'biz/attendance', 'C', '0', 'biz:attendance:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('考勤查询', @menuId, '1', 'F', '0', 'biz:attendance:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('考勤新增', @menuId, '2', 'F', '0', 'biz:attendance:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('考勤修改', @menuId, '3', 'F', '0', 'biz:attendance:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('考勤删除', @menuId, '4', 'F', '0', 'biz:attendance:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('请假', @dirHr, '1', 'biz/leave', 'C', '0', 'biz:leave:view', '#', 'admin', sysdate());
set @menuId = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假查询', @menuId, '1', 'F', '0', 'biz:leave:list', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假新增', @menuId, '2', 'F', '0', 'biz:leave:add', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假修改', @menuId, '3', 'F', '0', 'biz:leave:edit', 'admin', sysdate());
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假删除', @menuId, '4', 'F', '0', 'biz:leave:remove', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假审批', @menuId, '5', 'F', '0', 'biz:leave:audit', 'admin', sysdate());

-- ----------------------------
-- 业务字典
-- ----------------------------
insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('合同状态', 'biz_contract_status', '0', 'admin', sysdate(), '合同状态列表');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '草稿', '0', 'biz_contract_status', '0', 'admin', sysdate()),
       (2, '执行中', '1', 'biz_contract_status', '0', 'admin', sysdate()),
       (3, '已完成', '2', 'biz_contract_status', '0', 'admin', sysdate()),
       (4, '已终止', '3', 'biz_contract_status', '0', 'admin', sysdate());

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('出入库状态', 'biz_inout_status', '0', 'admin', sysdate(), '采购/销售单出入库状态');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '待处理', '0', 'biz_inout_status', '0', 'admin', sysdate()),
       (2, '已完成', '1', 'biz_inout_status', '0', 'admin', sysdate());

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('考勤状态', 'biz_attendance_status', '0', 'admin', sysdate(), '考勤状态列表');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '正常', '0', 'biz_attendance_status', '0', 'admin', sysdate()),
       (2, '迟到', '1', 'biz_attendance_status', '0', 'admin', sysdate()),
       (3, '早退', '2', 'biz_attendance_status', '0', 'admin', sysdate()),
       (4, '缺勤', '3', 'biz_attendance_status', '0', 'admin', sysdate());

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('请假类型', 'biz_leave_type', '0', 'admin', sysdate(), '请假类型列表');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '事假', '1', 'biz_leave_type', '0', 'admin', sysdate()),
       (2, '病假', '2', 'biz_leave_type', '0', 'admin', sysdate()),
       (3, '年假', '3', 'biz_leave_type', '0', 'admin', sysdate()),
       (4, '调休', '4', 'biz_leave_type', '0', 'admin', sysdate());

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('请假审批状态', 'biz_leave_status', '0', 'admin', sysdate(), '请假审批状态列表');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '待审批', '0', 'biz_leave_status', '0', 'admin', sysdate()),
       (2, '已通过', '1', 'biz_leave_status', '0', 'admin', sysdate()),
       (3, '已驳回', '2', 'biz_leave_status', '0', 'admin', sysdate());

-- ----------------------------
-- 数据看板菜单 + 各模块导出按钮权限（拓展功能）
-- ----------------------------
insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
select '数据看板', menu_id, 0, 'biz/dashboard', 'C', '0', 'biz:dashboard:view', 'fa fa-dashboard', 'admin', sysdate()
from sys_menu where menu_name='企业管理' and menu_type='M' limit 1;

insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
select concat(m.menu_name,'导出'), m.menu_id, 5, 'F', '0', concat(substring_index(m.perms, ':view', 1), ':export'), 'admin', sysdate()
from sys_menu m
where m.menu_type='C' and m.perms like 'biz:%:view' and m.parent_id in (
  select menu_id from (select menu_id from sys_menu where menu_name in ('客户合同产品','进销存管理','人事考勤')) t);

-- ----------------------------
-- 员工工作台（打卡/请销假/业务汇报）
-- ----------------------------
drop table if exists biz_report;
create table biz_report (
  report_id       bigint(20)      not null auto_increment    comment '主键ID',
  report_type     char(1)         default '1'                comment '汇报类型（1日报 2周报 3月报）',
  title           varchar(200)    default ''                 comment '标题',
  content         text                                       comment '汇报内容',
  report_date     varchar(20)     default ''                 comment '汇报日期',
  create_by       varchar(64)     default ''                 comment '创建者',
  create_time     datetime                                   comment '创建时间',
  update_by       varchar(64)     default ''                 comment '更新者',
  update_time     datetime                                   comment '更新时间',
  remark          varchar(500)    default null               comment '备注',
  primary key (report_id)
) engine=innodb auto_increment=1 comment = '业务汇报表';

insert into sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
values ('汇报类型', 'biz_report_type', '0', 'admin', sysdate(), '业务汇报类型');
insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (1, '日报', '1', 'biz_report_type', '0', 'admin', sysdate()),
       (2, '周报', '2', 'biz_report_type', '0', 'admin', sysdate()),
       (3, '月报', '3', 'biz_report_type', '0', 'admin', sysdate());

insert into sys_dict_data (dict_sort, dict_label, dict_value, dict_type, status, create_by, create_time)
values (4, '已销假', '3', 'biz_leave_status', '0', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('员工工作台', '0', '2', '#', 'M', '0', '', 'fa fa-rocket', 'admin', sysdate());
set @portal = @@identity;

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('我的打卡', @portal, '1', 'portal', 'C', '0', 'portal:index:view', 'fa fa-clock-o', 'admin', sysdate());
set @m1 = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('打卡操作', @m1, '1', 'F', '0', 'portal:punch:add', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('我的请假', @portal, '2', 'portal/leave', 'C', '0', 'portal:leave:view', 'fa fa-calendar', 'admin', sysdate());
set @m2 = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('请假销假', @m2, '1', 'F', '0', 'portal:leave:add', 'admin', sysdate());

insert into sys_menu (menu_name, parent_id, order_num, url, menu_type, visible, perms, icon, create_by, create_time)
values ('业务汇报', @portal, '3', 'portal/report', 'C', '0', 'portal:report:view', 'fa fa-file-text-o', 'admin', sysdate());
set @m3 = @@identity;
insert into sys_menu (menu_name, parent_id, order_num, menu_type, visible, perms, create_by, create_time)
values ('汇报提交', @m3, '1', 'F', '0', 'portal:report:add', 'admin', sysdate());

insert into sys_role_menu (role_id, menu_id)
select 2, menu_id from sys_menu where perms like 'portal:%' or menu_name='员工工作台';
