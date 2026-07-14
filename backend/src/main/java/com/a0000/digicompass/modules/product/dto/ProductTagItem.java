package com.a0000.digicompass.modules.product.dto;

public record ProductTagItem(
        Long id,
        String tagType,
        String tagName,
        String tagValue,
        int sortOrder
) {
}

