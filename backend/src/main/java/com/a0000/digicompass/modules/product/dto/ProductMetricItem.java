package com.a0000.digicompass.modules.product.dto;

import java.math.BigDecimal;

public record ProductMetricItem(
        Long id,
        String metricKey,
        String metricLabel,
        String metricValue,
        BigDecimal numericValue,
        String unit,
        int sortOrder
) {
}
