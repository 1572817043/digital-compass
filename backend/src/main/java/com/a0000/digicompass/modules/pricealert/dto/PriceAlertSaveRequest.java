package com.a0000.digicompass.modules.pricealert.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record PriceAlertSaveRequest(
        @NotNull Long productId,
        @NotNull @DecimalMin(value = "0.01", message = "目标价必须大于0") BigDecimal targetPrice,
        @Pattern(regexp = "official|used|channel", message = "价格类型不支持")
        String priceType
) {
}
