package com.a0000.digicompass.modules.product.dto;

import com.a0000.digicompass.modules.product.entity.ProductImage;
import com.a0000.digicompass.modules.product.entity.ProductSpec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProductDetail(
        Long id,
        String name,
        String model,
        String summary,
        BigDecimal officialPrice,
        Integer score,
        int status,
        Long categoryId,
        String categoryName,
        String categoryCode,
        Long brandId,
        String brandName,
        List<ProductImage> images,
        List<ProductSpec> specs,
        List<ProductMetricItem> metrics,
        List<ProductTagItem> tags,
        List<PriceInfo> prices,
        List<PurchaseLinkInfo> purchaseLinks
) {
    public record PriceInfo(
            Long id,
            String priceType,
            String platform,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal avgPrice,
            Integer sampleCount,
            LocalDate referenceDate,
            String sourceType,
            String remark
    ) {}
    public record PurchaseLinkInfo(Long id, String platform, String linkType, String title, String url, int sortOrder) {}
}
