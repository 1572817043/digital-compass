package com.a0000.digicompass.modules.ai.dto;

public record AiTestResult(
        boolean success,
        String model,
        String responsePreview,
        String errorMessage
) {
}
