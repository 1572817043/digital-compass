package com.a0000.digicompass.modules.assistant.dto;

public record RecommendationCandidate(
        Long productId,
        String productName,
        int score,
        String reason
) {
}
