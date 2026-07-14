package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductImageBindRequest(
        @NotBlank String imageUrl,
        String imageType,
        Integer sortOrder
) {
}
