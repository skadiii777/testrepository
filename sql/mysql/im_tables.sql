-- ============================================================
-- IM 即时通讯（移植自 yudao-module-im）
-- 1) 17 张业务表（由上游单测建表脚本转换，幂等 CREATE IF NOT EXISTS）
-- 2) 字典（提取自上游种子，INSERT IGNORE 幂等；已剔除 highgo 特有 deleted_time 列）
-- 3) 启用 IM 菜单子树（1418 及其子级、im/ 组件页）
-- ============================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `im_private_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `client_message_id` varchar(64) DEFAULT NULL COMMENT '客户端消息编号',
    `sender_id` bigint NOT NULL COMMENT '发送人编号',
    `receiver_id` bigint NOT NULL COMMENT '接收人编号',
    `type` smallint NOT NULL COMMENT '消息类型',
    `content` text DEFAULT NULL COMMENT '消息内容',
    `status` tinyint NOT NULL COMMENT '消息状态',
    `receipt_status` tinyint NOT NULL DEFAULT 0 COMMENT '回执状态',
    `send_time` datetime NOT NULL COMMENT '发送时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_private_message_sender_client` (`sender_id`, `client_message_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 私聊消息表';

CREATE TABLE IF NOT EXISTS `im_group_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `client_message_id` varchar(64) DEFAULT NULL COMMENT '客户端消息编号',
    `sender_id` bigint NOT NULL COMMENT '发送人编号',
    `group_id` bigint NOT NULL COMMENT '群编号',
    `type` smallint NOT NULL COMMENT '消息类型',
    `content` text DEFAULT NULL COMMENT '消息内容',
    `status` tinyint NOT NULL COMMENT '消息状态',
    `send_time` datetime NOT NULL COMMENT '发送时间',
    `receiver_user_ids` text DEFAULT NULL COMMENT '定向接收用户编号列表',
    `at_user_ids` varchar(1024) DEFAULT NULL COMMENT '@ 目标用户编号列表',
    `receipt_status` tinyint NOT NULL DEFAULT 0 COMMENT '回执状态',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_group_message_sender_client` (`sender_id`, `client_message_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 群聊消息表';

CREATE TABLE IF NOT EXISTS `im_group` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(64) NOT NULL COMMENT '群名称',
    `owner_user_id` bigint NOT NULL COMMENT '群主用户编号',
    `avatar` varchar(512) DEFAULT NULL COMMENT '群头像',
    `notice` varchar(2048) DEFAULT NULL COMMENT '群公告',
    `banned` bit(1) DEFAULT b'0' COMMENT '是否封禁',
    `banned_reason` varchar(512) DEFAULT NULL COMMENT '封禁原因',
    `banned_time` datetime DEFAULT NULL COMMENT '封禁时间',
    `status` tinyint NOT NULL COMMENT '群状态',
    `dissolved_time` datetime DEFAULT NULL COMMENT '解散时间',
    `muted_all` bit(1) DEFAULT b'0' COMMENT '是否全群禁言',
    `join_approval` bit(1) NOT NULL DEFAULT b'0' COMMENT '进群是否需群主 / 管理员审批；false 自由进群，true 需审批',
    `pinned_message_ids` varchar(128) DEFAULT NULL COMMENT '群置顶消息编号列表，逗号分隔',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 群信息表';

CREATE TABLE IF NOT EXISTS `im_group_member` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `group_id` bigint NOT NULL COMMENT '群编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `display_user_name` varchar(64) DEFAULT NULL COMMENT '组内显示名',
    `group_remark` varchar(64) DEFAULT NULL COMMENT '群备注',
    `silent` bit(1) DEFAULT b'0' COMMENT '是否免打扰',
    `status` tinyint NOT NULL COMMENT '成员状态',
    `role` tinyint NOT NULL DEFAULT 3 COMMENT '成员角色：1=群主 2=管理员 3=普通成员',
    `join_time` datetime DEFAULT NULL COMMENT '入群时间',
    `add_source` tinyint DEFAULT NULL COMMENT '加入来源',
    `inviter_user_id` bigint DEFAULT NULL COMMENT '邀请人用户编号；用户主动申请进群时为 NULL',
    `quit_time` datetime DEFAULT NULL COMMENT '退群时间',
    `mute_end_time` datetime DEFAULT NULL COMMENT '禁言到期时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_group_member` (`group_id`, `user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 群成员表';

CREATE TABLE IF NOT EXISTS `im_friend` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `friend_user_id` bigint NOT NULL COMMENT '好友用户编号',
    `silent` bit(1) DEFAULT b'0' COMMENT '是否免打扰',
    `display_name` varchar(64) NOT NULL DEFAULT '' COMMENT '好友展示备注（仅自己可见）',
    `add_source` tinyint DEFAULT NULL COMMENT '添加来源',
    `pinned` bit(1) DEFAULT b'0' COMMENT '是否置顶联系人',
    `blocked` bit(1) DEFAULT b'0' COMMENT '是否拉黑',
    `status` tinyint NOT NULL COMMENT '好友状态',
    `add_time` datetime DEFAULT NULL COMMENT '添加好友时间',
    `delete_time` datetime DEFAULT NULL COMMENT '删除好友时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 好友关系表';

CREATE TABLE IF NOT EXISTS `im_friend_request` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `from_user_id` bigint NOT NULL COMMENT '发起方用户编号',
    `to_user_id` bigint NOT NULL COMMENT '接收方用户编号',
    `handle_result` tinyint NOT NULL DEFAULT 0 COMMENT '处理结果；0未处理；1同意；2拒绝',
    `apply_content` varchar(255) DEFAULT NULL COMMENT '申请理由',
    `handle_content` varchar(255) DEFAULT NULL COMMENT '处理理由',
    `display_name` varchar(64) DEFAULT NULL COMMENT '发起方对接收方的备注',
    `add_source` tinyint DEFAULT NULL COMMENT '添加来源',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_friend_request` (`from_user_id`, `to_user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 好友申请记录表';

CREATE TABLE IF NOT EXISTS `im_group_request` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `group_id` bigint NOT NULL COMMENT '群编号',
    `user_id` bigint NOT NULL COMMENT '申请人 / 被邀请人用户编号',
    `inviter_user_id` bigint DEFAULT NULL COMMENT '邀请人用户编号；NULL=主动申请；非NULL=被邀请待审批',
    `apply_content` varchar(255) DEFAULT NULL COMMENT '申请理由',
    `add_source` tinyint DEFAULT NULL COMMENT '加入来源',
    `handle_result` tinyint NOT NULL DEFAULT 0 COMMENT '处理结果；0未处理；1同意；2拒绝',
    `handle_user_id` bigint DEFAULT NULL COMMENT '处理人用户编号',
    `handle_content` varchar(255) DEFAULT NULL COMMENT '处理理由',
    `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_group_request` (`group_id`, `user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 加群申请记录表';

CREATE TABLE IF NOT EXISTS `im_face_pack` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(64) NOT NULL COMMENT '表情包名称',
    `icon` varchar(512) DEFAULT NULL COMMENT '表情包图标（面板底部 tab 显示）',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL COMMENT '状态',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 表情包表';

CREATE TABLE IF NOT EXISTS `im_face_pack_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `pack_id` bigint NOT NULL COMMENT '所属表情包编号',
    `url` varchar(512) NOT NULL COMMENT '表情图 URL',
    `name` varchar(64) DEFAULT NULL COMMENT '表情名（可选；如「狗头」「捂脸」）',
    `width` int NOT NULL DEFAULT 0 COMMENT '渲染宽度（像素）',
    `height` int NOT NULL DEFAULT 0 COMMENT '渲染高度（像素）',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL COMMENT '状态',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 表情包项表';

CREATE TABLE IF NOT EXISTS `im_rtc_call` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `room` varchar(64) NOT NULL COMMENT '业务通话编号',
    `conversation_type` tinyint NOT NULL COMMENT '会话类型',
    `media_type` tinyint NOT NULL COMMENT '媒体类型',
    `inviter_user_id` bigint NOT NULL COMMENT '发起人用户编号',
    `group_id` bigint DEFAULT NULL COMMENT '群编号',
    `status` tinyint NOT NULL COMMENT '通话状态',
    `end_reason` tinyint DEFAULT NULL COMMENT '结束原因',
    `start_time` datetime NOT NULL COMMENT '发起时间',
    `accept_time` datetime DEFAULT NULL COMMENT '接通时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 通话记录表';

CREATE TABLE IF NOT EXISTS `im_rtc_participant` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `call_id` bigint NOT NULL COMMENT '通话编号',
    `room` varchar(64) NOT NULL COMMENT '业务通话编号',
    `user_id` bigint NOT NULL COMMENT '参与者用户编号',
    `role` tinyint NOT NULL COMMENT '参与角色',
    `status` tinyint NOT NULL COMMENT '参与状态',
    `invite_time` datetime NOT NULL COMMENT '被邀请时间',
    `accept_time` datetime DEFAULT NULL COMMENT '接听时间',
    `leave_time` datetime DEFAULT NULL COMMENT '离开时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_rtc_participant_room_user` (`room`, `user_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 通话参与者表';

CREATE TABLE IF NOT EXISTS `im_face_user_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '所属用户编号',
    `url` varchar(512) NOT NULL COMMENT '表情图 URL',
    `name` varchar(64) DEFAULT NULL COMMENT '表情名（可选）',
    `width` int NOT NULL DEFAULT 0 COMMENT '渲染宽度（像素）',
    `height` int NOT NULL DEFAULT 0 COMMENT '渲染高度（像素）',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_face_user_item_user_url_deleted` (`user_id`, `url`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 用户私有表情表';

CREATE TABLE IF NOT EXISTS `im_channel` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `code` varchar(64) NOT NULL COMMENT '频道业务码；唯一',
    `name` varchar(64) NOT NULL COMMENT '频道名称',
    `avatar` varchar(512) DEFAULT NULL COMMENT '频道头像',
    `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态；0 启用 1 停用',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 频道表';

CREATE TABLE IF NOT EXISTS `im_channel_material` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `channel_id` bigint NOT NULL COMMENT '频道编号',
    `type` tinyint NOT NULL COMMENT '内容类型；1 站内富文本 2 外链',
    `title` varchar(128) NOT NULL COMMENT '标题',
    `cover_url` varchar(512) DEFAULT NULL COMMENT '封面图',
    `summary` varchar(255) DEFAULT NULL COMMENT '摘要',
    `content` text DEFAULT NULL COMMENT '正文；富文本 HTML',
    `url` varchar(512) DEFAULT NULL COMMENT '跳转链接；为空时点击在客户端内置详情页拉 content；非空则跳 url',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 频道素材表';

CREATE TABLE IF NOT EXISTS `im_channel_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `channel_id` bigint NOT NULL COMMENT '频道编号；冗余 im_channel_material.channel_id 便于检索',
    `material_id` bigint NOT NULL COMMENT '关联素材编号',
    `type` smallint NOT NULL COMMENT '消息类型',
    `content` text DEFAULT NULL COMMENT '消息内容；推送时 payload JSON 快照；不含富文本正文',
    `receiver_user_ids` text DEFAULT NULL COMMENT '接收人编号列表；逗号分隔；为空表示全员',
    `send_time` datetime NOT NULL COMMENT '发送时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 频道消息表';

CREATE TABLE IF NOT EXISTS `im_conversation_read` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` bigint NOT NULL COMMENT '用户编号',
    `conversation_type` tinyint NOT NULL COMMENT '会话类型',
    `target_id` bigint NOT NULL COMMENT '目标编号',
    `message_id` bigint NOT NULL COMMENT '最大已读消息编号',
    `read_time` datetime NOT NULL COMMENT '最近已读时间',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_conversation_read_user_target` (`user_id`, `conversation_type`, `target_id`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 会话读位置表';

CREATE TABLE IF NOT EXISTS `im_sensitive_word` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `word` varchar(128) NOT NULL COMMENT '敏感词',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态；0 启用 1 停用',
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_im_sensitive_word` (`word`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 敏感词表';

CREATE TABLE IF NOT EXISTS `system_users` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(30) NOT NULL DEFAULT '',
    `password` varchar(100) NOT NULL DEFAULT '',
    `nickname` varchar(30) NOT NULL DEFAULT '',
    `remark` varchar(500) DEFAULT NULL,
    `dept_id` bigint DEFAULT NULL,
    `post_ids` varchar(255) DEFAULT NULL,
    `email` varchar(50) DEFAULT '',
    `mobile` varchar(11) DEFAULT '',
    `sex` tinyint DEFAULT 0,
    `avatar` varchar(100) DEFAULT '',
    `status` tinyint NOT NULL DEFAULT 0,
    `login_ip` varchar(50) DEFAULT '',
    `login_date` datetime DEFAULT NULL,
    `creator` varchar(64) DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT '',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` bit(1) NOT NULL DEFAULT b'0',
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3520, 1, '未读', '0', 'im_private_message_status', 0, 'warning', '', '私聊=未读，群聊=正常', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3521, 2, '已撤回', '2', 'im_private_message_status', 0, 'danger', '', 'RECALL', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3522, 3, '已读', '3', 'im_private_message_status', 0, 'success', '', 'READ（仅私聊）', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3525, 1, '正常', '0', 'im_group_message_status', 0, 'success', '', '群聊正常（初始状态）', 'admin', '2026-04-30 15:14:36', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3526, 2, '已撤回', '2', 'im_group_message_status', 0, 'danger', '', '群聊已撤回', 'admin', '2026-04-30 15:14:36', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3530, 1, '不需要回执', '0', 'im_group_message_receipt_status', 0, 'info', '', 'NO_RECEIPT', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3531, 2, '待完成', '1', 'im_group_message_receipt_status', 0, 'warning', '', 'PENDING', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3532, 3, '已完成', '2', 'im_group_message_receipt_status', 0, 'success', '', 'DONE', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3540, 1, '正常', '0', 'im_friend_status', 0, 'success', '', '正常好友关系', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3541, 2, '已删除', '1', 'im_friend_status', 0, 'danger', '', '已删除好友关系', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3550, 1, '正常', '0', 'im_group_status', 0, 'success', '', '群正常', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3551, 2, '已解散', '1', 'im_group_status', 0, 'info', '', '群已解散', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3552, 1, '群主', '1', 'im_group_member_role', 0, 'primary', '', NULL, '1', '2026-05-02 02:14:12', '1', '2026-05-02 02:14:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3553, 2, '管理员', '2', 'im_group_member_role', 0, 'warning', '', NULL, '1', '2026-05-02 02:14:12', '1', '2026-05-02 02:14:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3554, 3, '普通成员', '3', 'im_group_member_role', 0, 'info', '', NULL, '1', '2026-05-02 02:14:12', '1', '2026-05-02 02:14:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3555, 1, '搜索', '1', 'im_friend_add_source', 0, 'default', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-05 11:46:57', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3556, 2, '群聊', '2', 'im_friend_add_source', 0, 'default', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-05 11:46:53', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3557, 3, '扫码', '3', 'im_friend_add_source', 0, 'default', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-05 11:46:50', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3558, 4, '名片', '4', 'im_friend_add_source', 0, 'default', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-05 11:46:48', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3559, 1, '等待验证', '0', 'im_friend_request_handle_result', 0, 'warning', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-04 02:43:41', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3560, 2, '已添加', '1', 'im_friend_request_handle_result', 0, 'success', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-04 02:43:41', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3561, 3, '已拒绝', '2', 'im_friend_request_handle_result', 0, 'info', '', NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-04 02:43:41', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3562, 101, '文本', '101', 'im_message_type', 0, '', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3563, 102, '图片', '102', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3564, 103, '语音', '103', 'im_message_type', 0, 'warning', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3565, 104, '视频', '104', 'im_message_type', 0, 'warning', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3566, 105, '文件', '105', 'im_message_type', 0, 'info', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3568, 2101, '撤回', '2101', 'im_message_type', 0, 'danger', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3569, 2200, '回执', '2200', 'im_message_type', 0, 'warning', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3570, 2201, '已读', '2201', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3571, 1501, '群创建', '1501', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3572, 1502, '群信息变更', '1502', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3573, 1503, '入群申请', '1503', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3574, 1504, '成员退群', '1504', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3575, 1505, '入群申请通过', '1505', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3576, 1506, '入群申请拒绝', '1506', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3577, 1507, '群主转让', '1507', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3578, 1508, '成员被移出', '1508', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3579, 1509, '成员加入', '1509', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3580, 1510, '自由进群', '1510', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3581, 1511, '群解散', '1511', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3582, 1512, '成员禁言', '1512', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3583, 1513, '成员取消禁言', '1513', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3584, 1514, '全群禁言', '1514', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3585, 1515, '全群取消禁言', '1515', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3586, 1516, '成员昵称变更', '1516', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3587, 1517, '添加管理员', '1517', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3588, 1518, '撤销管理员', '1518', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3589, 1519, '群公告变更', '1519', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3590, 1520, '群名变更', '1520', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3591, 1531, '群消息置顶', '1531', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3592, 1532, '群消息取消置顶', '1532', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3593, 1533, '群封禁变更', '1533', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 11:52:30', 'admin', '2026-05-05 11:52:30', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3594, 1204, '新增好友', '1204', 'im_message_type', 0, 'success', '', NULL, 'admin', '2026-05-05 13:26:53', 'admin', '2026-05-05 13:26:53', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3595, 1205, '好友被删除', '1205', 'im_message_type', 0, 'warning', '', NULL, 'admin', '2026-05-05 13:26:53', 'admin', '2026-05-05 13:26:53', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3596, 1, '搜索', '1', 'im_group_add_source', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3597, 2, '邀请', '2', 'im_group_add_source', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3598, 3, '扫码', '3', 'im_group_add_source', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3599, 4, '分享链接', '4', 'im_group_add_source', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3600, 1, '未处理', '0', 'im_group_request_handle_result', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3601, 2, '同意', '1', 'im_group_request_handle_result', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (3602, 3, '拒绝', '2', 'im_group_request_handle_result', 0, '', '', NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061096, 1, '语音', '1', 'im_rtc_call_media_type', 0, '', '', '语音通话', 'admin', '2026-05-16 11:34:50', 'admin', '2026-05-16 11:34:50', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061097, 2, '视频', '2', 'im_rtc_call_media_type', 0, '', '', '视频通话', 'admin', '2026-05-16 11:34:50', 'admin', '2026-05-16 11:34:50', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061098, 1, '私聊', '1', 'im_rtc_call_conversation_type', 0, 'primary', '', '一对一私聊通话', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061099, 2, '群聊', '2', 'im_rtc_call_conversation_type', 0, 'success', '', '群内多人通话', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061100, 1, '创建', '10', 'im_rtc_call_status', 0, 'info', '', '通话已创建，等待接通', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061101, 2, '进行中', '20', 'im_rtc_call_status', 0, 'primary', '', '已有人接通，通话中', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061102, 3, '已结束', '30', 'im_rtc_call_status', 0, 'success', '', '通话结束', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061103, 1, '通话结束', '1', 'im_rtc_call_end_reason', 0, 'success', '', '接通后任一方主动挂断', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061104, 2, '已拒绝', '2', 'im_rtc_call_end_reason', 0, 'warning', '', '被叫接通前点拒接', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061105, 3, '已取消', '3', 'im_rtc_call_end_reason', 0, 'info', '', '主叫接通前主动取消', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061106, 4, '无人接听', '4', 'im_rtc_call_end_reason', 0, 'info', '', '振铃超时未接通', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061107, 5, '对方正忙', '5', 'im_rtc_call_end_reason', 0, 'warning', '', '对方在另一通话中', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061108, 6, '通话异常', '9', 'im_rtc_call_end_reason', 0, 'danger', '', '网络中断、设备失败等', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061109, 1, '发起人', '1', 'im_rtc_participant_role', 0, 'primary', '', '通话发起者', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061110, 2, '被邀请者', '2', 'im_rtc_participant_role', 0, 'info', '', '被邀请加入', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061111, 3, '主动加入者', '3', 'im_rtc_participant_role', 0, 'success', '', '群通话场景，旁观者主动加入', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061112, 1, '邀请中', '10', 'im_rtc_participant_status', 0, 'info', '', '已发出 invite，等待响应', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061113, 2, '已加入', '20', 'im_rtc_participant_status', 0, 'primary', '', '已接通并进入房间', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061114, 3, '已拒绝', '30', 'im_rtc_participant_status', 0, 'warning', '', '接通前点拒接', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061115, 4, '未应答', '40', 'im_rtc_participant_status', 0, 'info', '', '通话已结束仍未应答', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061116, 5, '已离开', '50', 'im_rtc_participant_status', 0, 'success', '', '接通后挂断 / 离开', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061117, 1610, '通话开始', '1610', 'im_message_type', 0, 'info', '', '入消息流；私聊定向通知，群聊全员广播', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061118, 1611, '通话结束', '1611', 'im_message_type', 0, 'info', '', '入消息流；私聊准气泡，群聊系统 tip', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061119, 125, '素材', '125', 'im_message_type', 0, 'success', '', '频道运营推送的图文卡片消息', '1', '2026-05-18 13:14:34', '1', '2026-05-18 13:14:34', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061120, 1, '富文本', '1', 'im_channel_material_type', 0, 'primary', '', '', '1', '2026-05-19 14:09:25', '1', '2026-05-19 14:09:25', '0');
INSERT IGNORE INTO system_dict_data (id, sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted) VALUES (1061121, 2, '外链', '2', 'im_channel_material_type', 0, 'info', '', '', '1', '2026-05-19 14:09:25', '1', '2026-05-19 14:09:25', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2200, 'IM 消息类型', 'im_message_type', 0, '对应 ImMessageTypeEnum', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2201, 'IM 私聊消息状态', 'im_private_message_status', 0, '对应 ImMessageStatusEnum；私聊 0=未读 / 2=已撤回 / 3=已读', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2202, 'IM 群消息回执状态', 'im_group_message_receipt_status', 0, '对应 ImGroupMessageReceiptStatusEnum', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2203, 'IM 好友状态', 'im_friend_status', 0, '0=正常 / 1=已删除', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2204, 'IM 群状态', 'im_group_status', 0, '0=正常 / 1=已解散', 'admin', '2026-04-30 11:35:07', 'admin', '2026-04-30 11:35:07', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2205, 'IM 群聊消息状态', 'im_group_message_status', 0, '对应 ImMessageStatusEnum；群聊 0=正常 / 2=已撤回（无未读概念）', 'admin', '2026-04-30 15:14:36', 'admin', '2026-04-30 15:14:36', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2206, 'IM 群成员角色', 'im_group_member_role', 0, NULL, '1', '2026-05-02 02:14:12', '1', '2026-05-02 02:14:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2207, 'IM 好友添加来源', 'im_friend_add_source', 0, NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-04 02:43:41', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2208, 'IM 好友申请处理结果', 'im_friend_request_handle_result', 0, NULL, '1', '2026-05-04 02:43:41', '1', '2026-05-04 02:43:41', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2209, 'IM 加群来源', 'im_group_add_source', 0, NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (2210, 'IM 加群申请处理结果', 'im_group_request_handle_result', 0, NULL, '', '2026-05-06 09:26:36', '', '2026-05-06 09:26:36', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061092, 'IM 通话媒体类型', 'im_rtc_call_media_type', 0, NULL, 'admin', '2026-05-16 11:34:50', 'admin', '2026-05-16 11:34:50', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061093, 'IM 通话会话类型', 'im_rtc_call_conversation_type', 0, '1=私聊；2=群聊', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061094, 'IM 通话状态', 'im_rtc_call_status', 0, '10=创建；20=进行中；30=已结束', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061095, 'IM 通话结束原因', 'im_rtc_call_end_reason', 0, '1=通话结束；2=已拒绝；3=已取消；4=无人接听；5=对方正忙；9=通话异常', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061096, 'IM 通话参与角色', 'im_rtc_participant_role', 0, '1=发起人；2=被邀请者；3=主动加入者', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061097, 'IM 通话参与状态', 'im_rtc_participant_status', 0, '10=邀请中；20=已加入；30=已拒绝；40=未应答；50=已离开', 'admin', '2026-05-18 03:36:12', 'admin', '2026-05-18 03:36:12', '0');
INSERT IGNORE INTO system_dict_type (id, name, type, status, remark, creator, create_time, updater, update_time, deleted) VALUES (1061098, 'IM 频道素材内容类型', 'im_channel_material_type', 0, '1=站内富文本 / 2=外链', '1', '2026-05-19 14:09:25', '1', '2026-05-19 14:09:25', '0');

-- ============================================================
-- 启用 IM 菜单子树（当初清理演示菜单时停用；幂等）
-- ============================================================
UPDATE system_menu SET `status` = 0 WHERE `id` = 1418;
UPDATE system_menu SET `status` = 0 WHERE `parent_id` = 1418;
UPDATE system_menu SET `status` = 0 WHERE `component` LIKE 'im/%';
UPDATE system_menu SET `status` = 0 WHERE `parent_id` IN (SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 1418 OR component LIKE 'im/%') t);


-- ============================================================
-- 启用 IM 菜单子树（当初清理演示菜单时停用；幂等）
-- ============================================================
UPDATE system_menu SET `status` = 0 WHERE `id` = 1418;
UPDATE system_menu SET `status` = 0 WHERE `parent_id` = 1418;
UPDATE system_menu SET `status` = 0 WHERE `component` LIKE 'im/%';
UPDATE system_menu SET `status` = 0 WHERE `parent_id` IN (SELECT id FROM (SELECT id FROM system_menu WHERE parent_id = 1418 OR component LIKE 'im/%') t);

-- ============================================================
-- 访问日志操作名扩长（IM 部分接口摘要超 50 字符导致日志写入 500；幂等）
-- ============================================================
ALTER TABLE `infra_api_access_log` MODIFY COLUMN `operate_name` varchar(255) NOT NULL DEFAULT '' COMMENT '操作名';
