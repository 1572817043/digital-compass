package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductTagSaveRequest(
        @NotBlank
        @Pattern(regexp = "selling_point|weakness|suitable|unsuitable|scene")
        String tagType,

        @NotBlank
        @Size(max = 80)
        String tagName,

        @Size(max = 500)
        String tagValue,

        Integer sortOrder
) {
}
