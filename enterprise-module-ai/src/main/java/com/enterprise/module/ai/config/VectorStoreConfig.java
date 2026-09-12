package com.enterprise.module.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 向量库配置（学习阶段：SimpleVectorStore，JSON 文件持久化，零外部依赖）
 *
 * 概念：VectorStore 是 RAG 的"记忆体"——存嵌入向量 + 原文，支持语义检索。
 * SimpleVectorStore 把所有向量放内存，序列化到 JSON 文件持久化。
 * 后期换 Chroma/pgvector/Milvus 只需换这个 Bean 的实现类，上层 Service 代码零改动。
 *
 * @author 企业管理平台
 */
@Configuration
public class VectorStoreConfig {

    private static final String STORE_FILE = "vector-store.json";

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(STORE_FILE);
        if (file.exists()) {
            store.load(file); // 启动时从 JSON 恢复已嵌入的文档
        }
        return store;
    }

}
