package com.a0000.digicompass.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceSaveRequest(
        @NotBlank @Pattern(regexp = "official|used|channel", message = "必须是 official、used 或 channel") String priceType,
        @NotBlank String platform,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal avgPrice,
        @Min(value = 0, message = "不能小于 0") Integer sampleCount,
        @NotNull LocalDate referenceDate,
        String sourceType,
        String remark
) {
}
