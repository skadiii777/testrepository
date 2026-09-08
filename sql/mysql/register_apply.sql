-- ============================================================
-- 注册审批 + 部门默认角色映射（预留架构）
-- 注册 = 提交待审批申请（不建账号不可登录）；管理员审批通过后创建正式账号、
-- 加入申请的部门/岗位，并按 biz_dept_role_map 部门映射分配角色（无映射回退
-- 「普通角色」common）。未来按部门/职位开放模块权限，在映射表扩展即可。
-- 执行前提：enterprise-biz.sql 已导入。
-- ============================================================

-- 1. 注册申请表
CREATE TABLE biz_register_apply (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`     varchar(30)  NOT NULL                COMMENT '用户账号',
  `password`     varchar(100) NOT NULL                COMMENT '密码摘要（审批后置空）',
  `nickname`     varchar(30)  DEFAULT NULL            COMMENT '用户昵称',
  `dept_id`      bigint       NOT NULL                COMMENT '申请部门',
  `post_id`      bigint       DEFAULT NULL            COMMENT '申请岗位',
  `status`       char(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0=待审批 1=已通过 2=已驳回）',
  `reject_reason` varchar(255) DEFAULT NULL           COMMENT '驳回原因',
  `audit_time`   varchar(20)  DEFAULT NULL            COMMENT '审批时间',
  `creator`      varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`      varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      bit(1)       NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`    bigint       NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='注册申请表';

-- 2. 部门 → 默认角色 映射表（预留：按部门/职位批量开放模块权限的挂载点）
CREATE TABLE biz_dept_role_map (
  `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dept_id`     bigint   NOT NULL                COMMENT '部门编号',
  `role_id`     bigint   NOT NULL                COMMENT '角色编号',
  `creator`     varchar(64)  DEFAULT ''          COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`     varchar(64)  DEFAULT ''          COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     bit(1)   NOT NULL DEFAULT b'0'   COMMENT '是否删除',
  `tenant_id`   bigint   NOT NULL DEFAULT 0      COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_dept` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='部门默认角色映射表';

-- 3. 管理端菜单：系统管理 → 注册审批
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '注册审批', 'biz:register-apply:query', 2, 30, id, 'register-apply', 'ep:checked', 'system/registerapply/index', 'RegisterApply', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '系统管理' AND type = 1 LIMIT 1;
SET @m = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('注册审批操作', 'biz:register-apply:audit', 3, 1, @m, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
