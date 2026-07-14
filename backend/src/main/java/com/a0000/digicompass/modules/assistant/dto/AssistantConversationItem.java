package com.a0000.digicompass.modules.assistant.dto;

import java.time.LocalDateTime;

public record AssistantConversationItem(
        Long id,
        String title,
        String lastMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
