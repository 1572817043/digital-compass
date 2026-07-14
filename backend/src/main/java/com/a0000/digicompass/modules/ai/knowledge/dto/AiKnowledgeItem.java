package com.a0000.digicompass.modules.ai.knowledge.dto;

public record AiKnowledgeItem(
        Long id,
        Long categoryId,
        Long productId,
        String title,
        String content,
        String knowledgeType,
        String tags,
        String source,
        int status
) {
}
