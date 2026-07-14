package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;

public record SpecSaveRequest(
        @NotBlank String specGroup,
        @NotBlank String specName,
        @NotBlank String specValue,
        Integer sortOrder
) {
}
