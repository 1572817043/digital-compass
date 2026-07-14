package com.a0000.digicompass.modules.assistant.dto;

import java.time.LocalDateTime;

public record ChatMessageItem(
        Long id,
        Long conversationId,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
