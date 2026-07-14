package com.a0000.digicompass.modules.market.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.market.dto.MarketPriceItem;
import com.a0000.digicompass.modules.market.dto.MarketProductPriceRecord;
import com.a0000.digicompass.modules.market.dto.MarketSummary;
import com.a0000.digicompass.modules.market.service.MarketService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/prices")
    public ApiResponse<List<MarketPriceItem>> prices(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priceType,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.success(marketService.listPrices(categoryId, brandId, keyword, priceType, sort));
    }

    @GetMapping("/products/{productId}/prices")
    public ApiResponse<List<MarketProductPriceRecord>> productPrices(@PathVariable Long productId) {
        return ApiResponse.success(marketService.listProductPrices(productId));
    }

    @GetMapping("/summary")
    public ApiResponse<MarketSummary> summary() {
        return ApiResponse.success(marketService.getSummary());
    }
}
