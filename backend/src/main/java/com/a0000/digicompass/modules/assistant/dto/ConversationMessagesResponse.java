package com.a0000.digicompass.modules.assistant.dto;

import java.util.List;

public record ConversationMessagesResponse(
        Long conversationId,
        List<ChatMessageItem> messages,
        List<AssistantRecommendationItem> recommendations
) {
}
