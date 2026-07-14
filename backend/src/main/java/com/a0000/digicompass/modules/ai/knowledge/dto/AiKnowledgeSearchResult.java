package com.a0000.digicompass.modules.ai.knowledge.dto;

public record AiKnowledgeSearchResult(
        Long chunkId,
        Long knowledgeId,
        Long productId,
        String title,
        String content,
        int score,
        String retrievalSource,
        String embeddingModel
) {
}
