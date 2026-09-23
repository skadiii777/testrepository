package com.enterprise.framework.ai.api;

import java.util.List;

/**
 * 可供业务模块调用的知识问答契约。实现负责当前租户上下文和知识库权限校验。
 */
public interface AiKnowledgeAssistant {

    /**
     * 基于指定知识库回答问题。知识库 ID 必须属于当前租户。
     */
    Answer answer(Long knowledgeBaseId, String question);

    record Answer(String answer, List<Citation> citations) {}

    record Citation(String documentId, String source) {}
}
