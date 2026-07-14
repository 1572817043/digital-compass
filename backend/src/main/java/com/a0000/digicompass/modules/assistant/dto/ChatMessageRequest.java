package com.a0000.digicompass.modules.assistant.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String content
) {
}
