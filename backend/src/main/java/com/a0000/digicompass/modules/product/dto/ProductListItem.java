package com.a0000.digicompass.modules.product.dto;

import java.math.BigDecimal;

public record ProductListItem(
        Long id,
        String name,
        String model,
        String summary,
        BigDecimal officialPrice,
        Integer score,
        Long categoryId,
        String categoryName,
        String categoryCode,
        Long brandId,
        String brandName,
        String coverUrl
) {
}
