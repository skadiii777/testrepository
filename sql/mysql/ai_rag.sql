-- Ollama + Qdrant RAG metadata. Apply manually after reviewing the deployment requirements.
-- Vector payloads remain in Qdrant; these tables track tenant-owned knowledge bases/documents.

CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `name` varchar(128) NOT NULL COMMENT '知识库名称',
  `description` varchar(500) NOT NULL DEFAULT '' COMMENT '知识库说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_ai_kb_tenant` (`tenant_id`, `deleted`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库';

CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `knowledge_base_id` bigint NOT NULL COMMENT '知识库ID',
  `document_uuid` varchar(36) NOT NULL COMMENT '向量库文档标识',
  `source_name` varchar(255) NOT NULL COMMENT '来源文件名',
  `status` varchar(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING/READY/FAILED',
  `chunk_count` int NOT NULL DEFAULT 0 COMMENT '文本块数量',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_doc_uuid` (`document_uuid`),
  KEY `idx_ai_doc_kb` (`tenant_id`, `knowledge_base_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识库文档';

