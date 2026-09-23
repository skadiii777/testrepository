package com.enterprise.module.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

/**
 * Qdrant 在模块开关开启时才连接；向量库生命周期独立于应用进程。
 *
 * @author 企业管理平台
 */
@Configuration
@ConditionalOnProperty(prefix = "enterprise.ai.rag", name = "enabled", havingValue = "true")
public class VectorStoreConfig {

    @Bean
    public QdrantClient ragQdrantClient(
            @Value("${enterprise.ai.rag.qdrant.host:127.0.0.1}") String host,
            @Value("${enterprise.ai.rag.qdrant.port:6334}") int port,
            @Value("${enterprise.ai.rag.qdrant.api-key:}") String apiKey,
            @Value("${enterprise.ai.rag.qdrant.tls:false}") boolean tls) {
        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, tls);
        if (apiKey != null && !apiKey.isBlank()) builder.withApiKey(apiKey);
        return new QdrantClient(builder.build());
    }

    @Bean("vectorStore")
    public VectorStore vectorStore(QdrantClient ragQdrantClient, EmbeddingModel embeddingModel,
                                   @Value("${enterprise.ai.rag.qdrant.collection:enterprise_knowledge}") String collection) {
        return org.springframework.ai.vectorstore.qdrant.QdrantVectorStore
                .builder(ragQdrantClient, embeddingModel)
                .collectionName(collection)
                .initializeSchema(true)
                .build();
    }

}
