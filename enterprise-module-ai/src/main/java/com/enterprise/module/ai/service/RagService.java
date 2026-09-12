package com.enterprise.module.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG（检索增强生成）核心服务
 *
 * 原理：用户提问 → 向量化 → 在向量库中找语义最相似的文本片段 →
 *       把片段作为上下文拼进提示词 → LLM 生成有依据的回答
 *
 * 两个核心方法：
 *   ingest()  —— "学习"：把文本切成块，逐块向量化后存入 Chroma
 *   chat()    —— "回答"：问题向量化 → 检索 → 拼上下文 → LLM 生成
 *
 * @author 企业管理平台
 */
@Service
public class RagService {

    /** 聊天模型（Ollama deepseek-r1） */
    private final ChatClient chatClient;
    /** 向量库（Chroma，存嵌入向量+原文） */
    private final VectorStore vectorStore;

    /** 检索返回的最相似片段数 */
    private static final int TOP_K = 3;
    /** RAG 提示词模板 */
    private static final String RAG_PROMPT = """
            你是一个企业管理知识助手。请仅根据以下参考信息回答用户的问题。
            如果参考信息中没有相关内容，请如实说"根据现有知识库，暂无相关信息"。

            参考信息：
            {context}

            用户问题：{question}
            """ ;

    public RagService(ChatClient.Builder chatClientBuilder, @Qualifier("vectorStore") VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    /**
     * 知识入库：把文本按段落切分，每段生成嵌入向量后存入 Chroma
     *
     * @param text   原始文本（如一篇制度文档）
     * @param docName 文档名称（存入元数据，便于追溯来源）
     * @return 切分后的块数
     */
    public int ingest(String text, String docName) {
        // 简单分块：按双换行拆段，每段不超过 500 字（实际项目用 TikToken 精确分块）
        List<Document> docs = chunkText(text, docName).stream()
                .map(chunk -> new Document(chunk, Map.of("source", docName)))
                .toList();
        vectorStore.add(docs);
        save();
        return docs.size();
    }

    /**
     * RAG 对话：检索最相关的片段，拼接上下文后由 LLM 生成回答
     *
     * @param question 用户问题
     * @return AI 回答
     */
    public String chat(String question) {
        // 1. 检索：把问题向量化，在 Chroma 里找语义最相似的 TOP_K 条
        List<Document> relevant = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(TOP_K).build());

        // 2. 拼接上下文
        String context = relevant.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        // 3. 调 LLM 生成
        return chatClient.prompt()
                .user(u -> u.text(RAG_PROMPT)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();
    }

    private void save() {
        if (vectorStore instanceof org.springframework.ai.vectorstore.SimpleVectorStore svs) {
            svs.save(new java.io.File("vector-store.json"));
        }
    }

    /**
     * 简单文本分块（按双换行拆段，超长段按 500 字符滑窗切）
     */
    private List<String> chunkText(String text, String docName) {
        List<String> chunks = new java.util.ArrayList<>();
        for (String para : text.split("\n\n")) {
            para = para.strip();
            if (para.isEmpty()) continue;
            if (para.length() <= 500) {
                chunks.add(para);
            } else {
                for (int i = 0; i < para.length(); i += 400) {
                    chunks.add(para.substring(i, Math.min(i + 500, para.length())));
                }
            }
        }
        return chunks;
    }

}
