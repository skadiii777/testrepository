package com.enterprise.module.ai.controller.admin;

import com.enterprise.module.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RAG 知识库问答
 *
 * @author 企业管理平台
 */
@Tag(name = "AI - RAG 知识库")
@RestController
@RequestMapping("/biz/ai/rag")
@Validated
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    @Operation(summary = "知识入库：切分→嵌入→存入向量库")
    public Map<String, Object> ingest(@RequestBody IngestReq req) {
        int chunks = ragService.ingest(req.text(), req.docName());
        return Map.of("chunks", chunks);
    }

    @PostMapping("/chat")
    @Operation(summary = "RAG 问答")
    public Map<String, String> chat(@RequestBody ChatReq req) {
        return Map.of("answer", ragService.chat(req.question()));
    }

    public record IngestReq(@NotEmpty(message = "文本不能为空") String text,
                            String docName) {}

    public record ChatReq(@NotEmpty(message = "问题不能为空") String question) {}

}
