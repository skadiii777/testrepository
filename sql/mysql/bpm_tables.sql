-- BPM 业务表（从 DO 推导，MySQL 版；含审计列 + tenant_id；列名驼峰转下划线）
-- BpmCategor
DROP TABLE IF EXISTS bpm_category;
CREATE TABLE bpm_category (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `sort` int DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_category';

-- BpmFor
DROP TABLE IF EXISTS bpm_form;
CREATE TABLE bpm_form (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `conf` varchar(255) DEFAULT NULL,
  `fields` varchar(2048) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_form';

-- BpmProcessDefinitionInf
DROP TABLE IF EXISTS bpm_process_definition_info;
CREATE TABLE bpm_process_definition_info (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_definition_id` varchar(255) DEFAULT NULL,
  `model_id` varchar(255) DEFAULT NULL,
  `model_type` int DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `form_type` int DEFAULT NULL,
  `form_id` bigint DEFAULT NULL,
  `form_conf` varchar(255) DEFAULT NULL,
  `form_fields` varchar(2048) DEFAULT NULL,
  `form_custom_create_path` varchar(255) DEFAULT NULL,
  `form_custom_view_path` varchar(255) DEFAULT NULL,
  `simple_model` varchar(255) DEFAULT NULL,
  `visible` bit(1) DEFAULT NULL,
  `sort` bigint DEFAULT NULL,
  `start_user_ids` varchar(2048) DEFAULT NULL,
  `start_dept_ids` varchar(2048) DEFAULT NULL,
  `manager_user_ids` varchar(2048) DEFAULT NULL,
  `allow_cancel_running_process` bit(1) DEFAULT NULL,
  `allow_withdraw_task` bit(1) DEFAULT NULL,
  `process_id_rule` varchar(255) DEFAULT NULL,
  `auto_approval_type` int DEFAULT NULL,
  `title_setting` varchar(255) DEFAULT NULL,
  `summary_setting` varchar(255) DEFAULT NULL,
  `process_before_trigger_setting` varchar(255) DEFAULT NULL,
  `process_after_trigger_setting` varchar(255) DEFAULT NULL,
  `task_before_trigger_setting` varchar(255) DEFAULT NULL,
  `task_after_trigger_setting` varchar(255) DEFAULT NULL,
  `print_template_setting` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_process_definition_info';

-- BpmProcessExpressio
DROP TABLE IF EXISTS bpm_process_expression;
CREATE TABLE bpm_process_expression (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `expression` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_process_expression';

-- BpmProcessListene
DROP TABLE IF EXISTS bpm_process_listener;
CREATE TABLE bpm_process_listener (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `event` varchar(255) DEFAULT NULL,
  `value_type` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_process_listener';

-- BpmUserGrou
DROP TABLE IF EXISTS bpm_user_group;
CREATE TABLE bpm_user_group (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `user_ids` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_user_group';

-- BpmOALeav
DROP TABLE IF EXISTS bpm_oa_leave;
CREATE TABLE bpm_oa_leave (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint DEFAULT NULL,
  `type` int DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `day` bigint DEFAULT NULL,
  `status` int DEFAULT NULL,
  `process_instance_id` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_oa_leave';

-- BpmProcessInstanceCop
DROP TABLE IF EXISTS bpm_process_instance_copy;
CREATE TABLE bpm_process_instance_copy (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `start_user_id` bigint DEFAULT NULL,
  `process_instance_name` varchar(255) DEFAULT NULL,
  `process_instance_id` varchar(255) DEFAULT NULL,
  `process_definition_id` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `activity_id` varchar(255) DEFAULT NULL,
  `activity_name` varchar(255) DEFAULT NULL,
  `task_id` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='bpm_process_instance_copy';
