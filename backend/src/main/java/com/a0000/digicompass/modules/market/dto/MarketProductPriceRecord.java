package com.a0000.digicompass.modules.market.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketProductPriceRecord(
        Long productId,
        String priceType,
        String platform,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal avgPrice,
        Integer sampleCount,
        LocalDate referenceDate,
        String sourceType,
        String remark
) {
}
