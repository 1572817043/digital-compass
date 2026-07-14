package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkSaveRequest(
        @NotBlank String platform,
        String linkType,
        @NotBlank String title,
        @NotBlank String url,
        Integer sortOrder,
        Integer enabled
) {
}
