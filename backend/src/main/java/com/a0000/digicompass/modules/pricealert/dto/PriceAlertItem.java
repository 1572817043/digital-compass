package com.a0000.digicompass.modules.pricealert.dto;

import java.math.BigDecimal;

public record PriceAlertItem(
        Long id,
        Long productId,
        String productName,
        String brandName,
        String categoryName,
        String categoryCode,
        String coverUrl,
        BigDecimal targetPrice,
        String priceType,
        String status,
        BigDecimal lastPrice,
        boolean triggered,
        String triggeredAt
) {
}
