package com.enterprise.module.ai.controller.admin;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.module.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Tag(name = "AI - RAG 知识库")
@RestController
@RequestMapping("/biz/ai/rag")
@Validated
@ConditionalOnProperty(prefix = "enterprise.ai.rag", name = "enabled", havingValue = "true")
public class RagController {

    private static final long MAX_UPLOAD_BYTES = 2L * 1024 * 1024;
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/knowledge-bases")
    @Operation(summary = "创建知识库")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:create')")
    public CommonResult<Map<String, Long>> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseReq req) {
        return CommonResult.success(Map.of("id", ragService.createKnowledgeBase(req.name(), req.description())));
    }

    @GetMapping("/knowledge-bases")
    @Operation(summary = "查询知识库")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:query')")
    public CommonResult<List<RagService.KnowledgeBase>> listKnowledgeBases() {
        return CommonResult.success(ragService.listKnowledgeBases());
    }

    @GetMapping("/knowledge-bases/{id}/documents")
    @Operation(summary = "查询知识库文档")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:query')")
    public CommonResult<List<RagService.KnowledgeDocument>> listDocuments(@PathVariable @Positive Long id) {
        return CommonResult.success(ragService.listDocuments(id));
    }

    @PostMapping(value = "/knowledge-bases/{id}/documents", consumes = "multipart/form-data")
    @Operation(summary = "上传文本或 Markdown 文档")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:create')")
    public CommonResult<Map<String, Object>> uploadDocument(@PathVariable @Positive Long id,
                                                              @RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.toLowerCase().endsWith(".txt") || filename.toLowerCase().endsWith(".md"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持 .txt 和 .md 文档");
        }
        if (file.isEmpty() || file.getSize() > MAX_UPLOAD_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件不能为空且不能超过 2 MiB");
        }
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            int chunks = ragService.ingest(text, filename, id);
            return CommonResult.success(Map.of("chunks", chunks));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无法读取上传文件", e);
        }
    }

    @DeleteMapping("/documents/{id}")
    @Operation(summary = "删除知识库文档")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:delete')")
    public CommonResult<Boolean> deleteDocument(@PathVariable @Positive Long id) {
        ragService.deleteDocument(id);
        return CommonResult.success(true);
    }

    @PostMapping("/chat")
    @Operation(summary = "RAG 问答")
    @PreAuthorize("@ss.hasPermission('ai:knowledge:query')")
    public CommonResult<RagService.ChatResult> chat(@Valid @RequestBody ChatReq req) {
        return CommonResult.success(ragService.chat(req.question(), req.knowledgeBaseId()));
    }

    public record KnowledgeBaseReq(@NotBlank String name, String description) {}
    public record ChatReq(@NotBlank String question, @NotNull @Positive Long knowledgeBaseId) {}
}
