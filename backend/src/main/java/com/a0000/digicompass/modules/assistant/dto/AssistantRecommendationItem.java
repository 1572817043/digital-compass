package com.a0000.digicompass.modules.assistant.dto;

import java.math.BigDecimal;
import java.util.List;

public record AssistantRecommendationItem(
        Long id,
        Long messageId,
        Long productId,
        String productName,
        String brandName,
        String categoryName,
        String coverUrl,
        BigDecimal officialPrice,
        BigDecimal usedMinPrice,
        BigDecimal usedMaxPrice,
        Integer productScore,
        Integer matchScore,
        String reason,
        String riskTip,
        String explainSummary,
        String detailPath,
        List<String> matchTags,
        List<String> cautionTags,
        List<String> nextActions,
        List<String> matchedRequirements,
        List<String> tradeoffNotes,
        List<String> knowledgeEvidence
) {
}
