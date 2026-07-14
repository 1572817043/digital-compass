package com.a0000.digicompass.modules.market.service;

import com.a0000.digicompass.modules.market.dto.MarketPriceItem;
import com.a0000.digicompass.modules.market.dto.MarketProductPriceRecord;
import com.a0000.digicompass.modules.market.dto.MarketSummary;
import java.util.List;

public interface MarketService {

    List<MarketPriceItem> listPrices(Long categoryId, Long brandId, String keyword, String priceType, String sort);

    List<MarketProductPriceRecord> listProductPrices(Long productId);

    MarketSummary getSummary();
}
