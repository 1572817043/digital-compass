package com.a0000.digicompass.modules.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record AiKnowledgeSaveRequest(
        Long categoryId,
        Long productId,
        @NotBlank String title,
        @NotBlank String content,
        String knowledgeType,
        String tags,
        String source,
        Integer status
) {
}
