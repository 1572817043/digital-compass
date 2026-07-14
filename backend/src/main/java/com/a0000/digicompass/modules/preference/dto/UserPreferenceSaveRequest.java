package com.a0000.digicompass.modules.preference.dto;

import java.math.BigDecimal;

public record UserPreferenceSaveRequest(
        BigDecimal minBudget,
        BigDecimal maxBudget,
        Long categoryId,
        String brandIds,
        String usageScenes,
        String priorityTags,
        String avoidTags,
        String remark
) {
}
