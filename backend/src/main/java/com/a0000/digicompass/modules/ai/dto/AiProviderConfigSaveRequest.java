package com.a0000.digicompass.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiProviderConfigSaveRequest(
        @NotBlank String providerCode,
        @NotBlank String providerName,
        String protocolType,
        @NotBlank String baseUrl,
        String apiKey,
        String chatModel,
        String embeddingModel,
        Double temperature,
        Integer maxTokens,
        Boolean millionContext,
        Integer timeoutSeconds,
        Boolean enabled,
        Boolean defaultProvider,
        String remark
) {
}
