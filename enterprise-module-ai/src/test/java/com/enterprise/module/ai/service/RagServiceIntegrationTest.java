package com.enterprise.module.ai.service;

import com.enterprise.framework.tenant.core.context.TenantContextHolder;
import com.enterprise.module.ai.config.VectorStoreConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in, real Ollama + Qdrant + MySQL RAG test. The DB URL and collection name are restricted to QA namespaces.
 */
@EnabledIfEnvironmentVariable(
        named = "RAG_TEST_JDBC_URL",
        matches = "jdbc:mysql://[^/]+/(?:enterprise_pro_qa_rag_[A-Za-z0-9_]+|enterprise-pro)(?:\\?.*)?"
)
@SpringBootTest(classes = RagServiceIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RagServiceIntegrationTest {

    @Autowired
    private RagService ragService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Environment environment;

    @DynamicPropertySource
    static void configureRagServices(DynamicPropertyRegistry registry) {
        registry.add("enterprise.info.base-package", () -> "com.enterprise.module.ai");
        registry.add("enterprise.ai.rag.enabled", () -> "true");
        registry.add("enterprise.ai.rag.qdrant.host", () -> "127.0.0.1");
        registry.add("enterprise.ai.rag.qdrant.port", () -> 6334);
        registry.add("enterprise.ai.rag.qdrant.tls", () -> false);
        registry.add("enterprise.ai.rag.qdrant.collection", RagServiceIntegrationTest::requireTestCollection);
        registry.add("spring.ai.ollama.base-url", () -> "http://127.0.0.1:11434");
        registry.add("spring.ai.ollama.chat.options.model", () -> "deepseek-r1:1.5b");
        registry.add("spring.ai.ollama.embedding.options.model", () -> "nomic-embed-text");
    }

    @Test
    void ingestsBusinessCorpusAnswersWithCitationAndKeepsKnowledgeBasesIsolated() throws Exception {
        String collection = environment.getProperty("enterprise.ai.rag.qdrant.collection");
        assertNotNull(collection);
        assertTrue(isLocalDevRun() ? collection.equals("enterprise_knowledge")
                        : collection.matches("enterprise_knowledge_qa_[a-z0-9_]+"),
                "Integration test may only use its QA collection or explicitly enabled local collection");

        TenantContextHolder.setTenantId(1L);
        String corpusPath = System.getenv().getOrDefault("RAG_TEST_CORPUS_PATH",
                "../docs/rag-test-corpus/企业业务流程测试资料.md");
        String corpus = Files.readString(Path.of(corpusPath));
        String knowledgeBaseName = isLocalDevRun() ? "企业业务流程测试知识库"
                : "RAG功能测试-销售采购" + System.currentTimeMillis();
        Long knowledgeBaseId = ragService.createKnowledgeBase(knowledgeBaseName, "销售、采购、请假、报销和库存流程测试资料");
        int chunks = ragService.ingest(corpus, "企业业务流程测试资料.md", knowledgeBaseId);

        assertTrue(chunks > 0, "business corpus should be split into vectors");
        var documents = ragService.listDocuments(knowledgeBaseId);
        assertEquals(1, documents.size());
        assertEquals("READY", documents.get(0).status());
        assertEquals(chunks, documents.get(0).chunkCount());

        RagService.ChatResult answer = ragService.chat("销售单创建后库存会马上减少吗？", knowledgeBaseId);
        assertFalse(answer.answer().isBlank());
        assertTrue(answer.citations().stream().anyMatch(c -> c.source().equals("企业业务流程测试资料.md")),
                "answer should cite the ingested business document");

        if (!isLocalDevRun()) {
            Long emptyKnowledgeBaseId = ragService.createKnowledgeBase("RAG功能测试-空库" + System.currentTimeMillis(),
                    "跨知识库隔离验证");
            RagService.ChatResult isolated = ragService.chat("销售单创建后库存会马上减少吗？", emptyKnowledgeBaseId);
            assertEquals("知识库中没有找到足够相关的依据。", isolated.answer());
            assertTrue(isolated.citations().isEmpty());
        }

        Integer persisted = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_knowledge_document " +
                "WHERE id = ? AND tenant_id = 1 AND knowledge_base_id = ? AND deleted = 0", Integer.class,
                documents.get(0).id(), knowledgeBaseId);
        assertEquals(1, persisted);
    }

    @AfterEach
    void clearTenant() {
        TenantContextHolder.clear();
    }

    private static String requireTestCollection() {
        String collection = System.getenv("RAG_TEST_QDRANT_COLLECTION");
        boolean allowed = isLocalDevRun() ? "enterprise_knowledge".equals(collection)
                : collection != null && collection.matches("enterprise_knowledge_qa_[a-z0-9_]+");
        if (!allowed) {
            throw new IllegalStateException("RAG test collection must use its QA namespace or explicitly enabled local collection");
        }
        return collection;
    }

    private static boolean isLocalDevRun() {
        return "true".equalsIgnoreCase(System.getenv("RAG_TEST_ALLOW_LOCAL_DEV_DB"));
    }

    private static boolean isAllowedDatabase(String url) {
        if (url == null) return false;
        if (url.matches("jdbc:mysql://[^/]+/enterprise_pro_qa_rag_[A-Za-z0-9_]+(?:\\?.*)?")) return true;
        return isLocalDevRun() && url.matches("jdbc:mysql://127\\.0\\.0\\.1:3306/enterprise-pro(?:\\?.*)?");
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(classes = {
            org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration.class,
            org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration.class,
            org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration.class
    })
    @EnableTransactionManagement
    @Import({RagService.class, VectorStoreConfig.class})
    static class TestApplication {

        @Bean
        DataSource dataSource() {
            String url = System.getenv("RAG_TEST_JDBC_URL");
            if (!isAllowedDatabase(url)) {
                throw new IllegalStateException("Integration test only permits QA databases or explicitly enabled local enterprise-pro");
            }
            return new org.springframework.jdbc.datasource.DriverManagerDataSource(url,
                    System.getenv("RAG_TEST_DB_USER"), System.getenv("RAG_TEST_DB_PASSWORD"));
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }
}
