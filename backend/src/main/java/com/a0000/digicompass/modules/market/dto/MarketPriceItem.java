package com.a0000.digicompass.modules.market.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketPriceItem(
        Long productId,
        String productName,
        String brandName,
        String categoryName,
        String categoryCode,
        String coverUrl,
        BigDecimal officialPrice,
        Integer score,
        String priceType,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal avgPrice,
        Integer sampleCount,
        LocalDate referenceDate
) {
}
