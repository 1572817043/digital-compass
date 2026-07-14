package com.a0000.digicompass.modules.product.entity;

public record Brand(
        Long id,
        String name,
        String logoUrl,
        int sortOrder
) {
}
