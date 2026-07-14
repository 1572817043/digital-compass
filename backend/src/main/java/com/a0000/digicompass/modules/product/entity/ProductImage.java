package com.a0000.digicompass.modules.product.entity;

public record ProductImage(
        Long id,
        Long productId,
        String imageUrl,
        String imageType,
        int sortOrder
) {
}
