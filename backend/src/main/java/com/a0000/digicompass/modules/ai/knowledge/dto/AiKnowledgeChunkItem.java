package com.a0000.digicompass.modules.ai.knowledge.dto;

public record AiKnowledgeChunkItem(
        Long id,
        Long knowledgeId,
        Long productId,
        Long categoryId,
        int chunkIndex,
        String title,
        String content,
        int charCount,
        int status
) {
}
