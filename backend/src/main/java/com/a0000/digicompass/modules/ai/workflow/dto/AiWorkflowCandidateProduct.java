package com.a0000.digicompass.modules.ai.workflow.dto;

import java.math.BigDecimal;

public record AiWorkflowCandidateProduct(
        Long id,
        String name,
        String brandName,
        String categoryName,
        String coverUrl,
        BigDecimal officialPrice,
        Integer score,
        String detailPath
) {
}
