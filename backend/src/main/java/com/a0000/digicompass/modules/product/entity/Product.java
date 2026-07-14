package com.a0000.digicompass.modules.product.entity;

import java.util.List;

public record Product(
        String id,
        String name,
        String brand,
        String category,
        String summary,
        String officialPrice,
        String usedPrice,
        int score,
        List<String> tags,
        List<ProductSpec> specs
) {
}
