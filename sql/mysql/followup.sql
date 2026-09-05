-- ----------------------------
-- 客户跟进记录（v5 功能补全 #4）
-- ----------------------------
DROP TABLE IF EXISTS biz_customer_followup;
CREATE TABLE biz_customer_followup (
  `id`            bigint        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `customer_name` varchar(100)  DEFAULT NULL           COMMENT '客户名称',
  `follow_time`   varchar(20)   DEFAULT NULL           COMMENT '跟进时间',
  `method`        char(1)       DEFAULT NULL           COMMENT '跟进方式（1=电话 2=上门 3=微信 4=邮件 5=其他）',
  `content`       varchar(1000) DEFAULT NULL           COMMENT '跟进内容',
  `next_date`     varchar(20)   DEFAULT NULL           COMMENT '下次跟进日期',
  `creator`       varchar(64)   DEFAULT ''             COMMENT '创建者',
  `create_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`       varchar(64)   DEFAULT ''             COMMENT '更新者',
  `update_time`   datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       bit(1)        NOT NULL DEFAULT b'0'  COMMENT '是否删除',
  `tenant_id`     bigint        NOT NULL DEFAULT 0     COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_customer` (`customer_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='客户跟进记录表';

-- 字典：跟进方式
INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('跟进方式', 'biz_followup_method', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');
INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted)
VALUES (1, '电话', '1', 'biz_followup_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (2, '上门', '2', 'biz_followup_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (3, '微信', '3', 'biz_followup_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (4, '邮件', '4', 'biz_followup_method', 0, '1', NOW(), '1', NOW(), b'0'),
       (5, '其他', '5', 'biz_followup_method', 0, '1', NOW(), '1', NOW(), b'0');

-- 菜单：客户合同产品 → 客户跟进
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
SELECT '客户跟进', 'biz:followup:query', 2, 5, id, 'followup', '#', 'biz/followup/index', 'CustomerFollowup', 0, '1', NOW(), '1', NOW(), b'0'
FROM system_menu WHERE name = '客户合同产品' AND type = 1 LIMIT 1;
SET @fm = LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, status, creator, create_time, updater, update_time, deleted)
VALUES ('跟进查询', 'biz:followup:query', 3, 1, @fm, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('跟进新增', 'biz:followup:create', 3, 2, @fm, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('跟进删除', 'biz:followup:delete', 3, 3, @fm, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0'),
       ('跟进导出', 'biz:followup:export', 3, 4, @fm, '', '#', '', '', 0, '1', NOW(), '1', NOW(), b'0');
