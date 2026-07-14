package com.a0000.digicompass.modules.assistant.dto;

import java.util.List;

public record AssistantChatResponse(
        Long conversationId,
        ChatMessageItem userMessage,
        ChatMessageItem assistantMessage,
        List<AssistantRecommendationItem> recommendations
) {
}
