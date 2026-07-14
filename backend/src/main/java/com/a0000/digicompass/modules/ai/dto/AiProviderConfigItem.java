package com.a0000.digicompass.modules.ai.dto;

public record AiProviderConfigItem(
        Long id,
        String providerCode,
        String providerName,
        String protocolType,
        String baseUrl,
        String maskedApiKey,
        String chatModel,
        String embeddingModel,
        Double temperature,
        Integer maxTokens,
        boolean millionContext,
        Integer timeoutSeconds,
        boolean enabled,
        boolean defaultProvider,
        String remark
) {
}
