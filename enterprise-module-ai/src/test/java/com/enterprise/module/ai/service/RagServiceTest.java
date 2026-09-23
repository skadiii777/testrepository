package com.enterprise.module.ai.service;

import com.enterprise.framework.ai.api.AiKnowledgeAssistant;
import com.enterprise.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RagServiceTest {

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private VectorStore vectorStore;
    private JdbcTemplate jdbcTemplate;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.setTenantId(17L);
        chatClientBuilder = mock(ChatClient.Builder.class);
        chatClient = mock(ChatClient.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        vectorStore = mock(VectorStore.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        ragService = new RagService(chatClientBuilder, vectorStore, jdbcTemplate);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(42L), eq(17L))).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void ingestStoresChunksWithServerTenantAndSelectedKnowledgeBase() {
        int chunks = ragService.ingest("销售单创建后是草稿。\n\n销售出库在完成时扣减库存。", "业务流程.md", 42L);

        assertEquals(2, chunks);
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        @SuppressWarnings("unchecked")
        List<Document> documents = (List<Document>) captor.getValue();
        assertEquals(2, documents.size());
        assertEquals("17", documents.get(0).getMetadata().get("tenantId"));
        assertEquals("42", documents.get(0).getMetadata().get("knowledgeBaseId"));
        assertEquals("业务流程.md", documents.get(0).getMetadata().get("source"));
        assertEquals(documents.get(0).getMetadata().get("documentId"),
                documents.get(1).getMetadata().get("documentId"));
    }

    @Test
    void chatFiltersByServerTenantAndKnowledgeBaseAndReturnsNoEvidenceWithoutCallingModel() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        AiKnowledgeAssistant assistant = ragService;
        AiKnowledgeAssistant.Answer result = assistant.answer(42L, "销售什么时候扣库存？");

        assertEquals("知识库中没有找到足够相关的依据。", result.answer());
        assertTrue(result.citations().isEmpty());
        var captor = org.mockito.ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        String filter = captor.getValue().getFilterExpression().toString();
        assertAll(
                () -> assertTrue(filter.contains("Key[key=tenantId]") && filter.contains("Value[value=17]")),
                () -> assertTrue(filter.contains("Key[key=knowledgeBaseId]") && filter.contains("Value[value=42]"))
        );
        verifyNoInteractions(chatClient);
    }

    @Test
    void chatRejectsKnowledgeBaseThatDoesNotBelongToCurrentTenant() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(42L), eq(17L))).thenReturn(0);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> ragService.chat("库存何时变化？", 42L));
        verifyNoInteractions(vectorStore);
    }

    @Test
    void ingestRejectsEmptyAndOversizedTextBeforeWriting() {
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> ragService.ingest("  ", "空文件.md", 42L));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> ragService.ingest("中".repeat(200_001), "超长.md", 42L));
        verify(vectorStore, never()).add(anyList());
    }
}
