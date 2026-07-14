package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductSaveRequest(
        @NotNull Long categoryId,
        @NotNull Long brandId,
        @NotBlank String name,
        String model,
        String summary,
        String coverUrl,
        java.math.BigDecimal officialPrice,
        Integer score,
        Integer status
) {
}
