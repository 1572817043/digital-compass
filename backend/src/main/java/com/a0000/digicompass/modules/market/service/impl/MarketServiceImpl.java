package com.a0000.digicompass.modules.market.service.impl;

import com.a0000.digicompass.modules.market.dto.MarketPriceItem;
import com.a0000.digicompass.modules.market.dto.MarketProductPriceRecord;
import com.a0000.digicompass.modules.market.dto.MarketSummary;
import com.a0000.digicompass.modules.market.mapper.MarketMapper;
import com.a0000.digicompass.modules.market.service.MarketService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketServiceImpl implements MarketService {

    private final MarketMapper marketMapper;

    public MarketServiceImpl(MarketMapper marketMapper) {
        this.marketMapper = marketMapper;
    }

    @Override
    public List<MarketPriceItem> listPrices(Long categoryId, Long brandId, String keyword, String priceType, String sort) {
        return marketMapper.findPrices(categoryId, brandId, keyword, priceType, sort);
    }

    @Override
    public List<MarketProductPriceRecord> listProductPrices(Long productId) {
        return marketMapper.findProductPrices(productId);
    }

    @Override
    public MarketSummary getSummary() {
        return marketMapper.getSummary();
    }
}
