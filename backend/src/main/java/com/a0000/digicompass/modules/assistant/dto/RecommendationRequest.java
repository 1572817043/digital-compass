package com.a0000.digicompass.modules.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public record RecommendationRequest(
        @NotBlank String requirement,
        Integer budget,
        String category
) {
}
