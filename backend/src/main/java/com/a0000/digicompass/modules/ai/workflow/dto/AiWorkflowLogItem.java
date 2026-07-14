package com.a0000.digicompass.modules.ai.workflow.dto;

import java.time.LocalDateTime;

public record AiWorkflowLogItem(
        Long id,
        Long userId,
        String username,
        Long conversationId,
        Long providerId,
        String providerName,
        String modelName,
        String userRequirement,
        String parsedRequirementJson,
        String retrievedContextSummary,
        String candidateProductIds,
        boolean fallbackUsed,
        String errorMessage,
        LocalDateTime createdAt
) {
}
