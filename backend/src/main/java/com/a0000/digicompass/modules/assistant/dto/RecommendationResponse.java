package com.a0000.digicompass.modules.assistant.dto;

import java.util.List;

public record RecommendationResponse(
        String summary,
        List<RecommendationCandidate> candidates,
        List<String> risks
) {
}
