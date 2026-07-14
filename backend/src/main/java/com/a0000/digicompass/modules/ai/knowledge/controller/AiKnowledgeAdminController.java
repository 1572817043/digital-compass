package com.a0000.digicompass.modules.ai.knowledge.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSaveRequest;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeEmbeddingService;
import com.a0000.digicompass.modules.ai.knowledge.service.AiKnowledgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ai/knowledge")
public class AiKnowledgeAdminController {

    private final AiKnowledgeService knowledgeService;
    private final AiKnowledgeEmbeddingService embeddingService;

    public AiKnowledgeAdminController(AiKnowledgeService knowledgeService, AiKnowledgeEmbeddingService embeddingService) {
        this.knowledgeService = knowledgeService;
        this.embeddingService = embeddingService;
    }

    @GetMapping
    public ApiResponse<List<AiKnowledgeItem>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String knowledgeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId
    ) {
        return ApiResponse.success(knowledgeService.listKnowledge(keyword, knowledgeType, status, categoryId, productId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AiKnowledgeItem> get(@PathVariable Long id) {
        return ApiResponse.success(knowledgeService.getKnowledge(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody AiKnowledgeSaveRequest request) {
        return ApiResponse.success(knowledgeService.createKnowledge(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AiKnowledgeSaveRequest request) {
        knowledgeService.updateKnowledge(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteKnowledge(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/chunks/rebuild")
    public ApiResponse<Void> rebuildChunks(@PathVariable Long id) {
        knowledgeService.rebuildChunks(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/chunks")
    public ApiResponse<List<AiKnowledgeChunkItem>> listChunks(@PathVariable Long id) {
        return ApiResponse.success(knowledgeService.listChunks(id));
    }

    @PostMapping("/products/rebuild")
    public ApiResponse<Integer> rebuildProductKnowledge() {
        return ApiResponse.success(knowledgeService.rebuildProductKnowledge());
    }

    @PostMapping("/embeddings/rebuild")
    public ApiResponse<Integer> rebuildEmbeddings() {
        return ApiResponse.success(embeddingService.rebuildAllEmbeddings());
    }

    @PostMapping("/{id}/embeddings/rebuild")
    public ApiResponse<Integer> rebuildKnowledgeEmbeddings(@PathVariable Long id) {
        return ApiResponse.success(embeddingService.rebuildKnowledgeEmbeddings(id));
    }

    @GetMapping("/embeddings/search")
    public ApiResponse<List<AiKnowledgeSearchResult>> searchEmbeddings(
            @RequestParam String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(embeddingService.search(query, categoryId, productId, limit));
    }
}
