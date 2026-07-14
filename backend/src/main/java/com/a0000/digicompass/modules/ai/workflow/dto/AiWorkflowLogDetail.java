package com.a0000.digicompass.modules.ai.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiWorkflowLogDetail(
        Long id,
        Long userId,
        String username,
        Long conversationId,
        Long providerId,
        String providerName,
        String modelName,
        String userRequirement,
        String parsedRequirementJson,
        Map<String, Object> parsedRequirement,
        String retrievedContextSummary,
        List<String> retrievedContexts,
        String candidateProductIdsText,
        List<Long> candidateProductIds,
        List<AiWorkflowCandidateProduct> candidateProducts,
        boolean fallbackUsed,
        String errorMessage,
        List<AiWorkflowStepItem> workflowSteps,
        LocalDateTime createdAt
) {
}
