package com.a0000.digicompass.modules.market.dto;

public record MarketSummary(
        int totalProducts,
        int productsWithPrice,
        int usedPriceCount,
        int recentUpdates
) {
}
