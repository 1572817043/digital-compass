package com.a0000.digicompass.modules.history.service.impl;

import com.a0000.digicompass.modules.history.mapper.HistoryMapper;
import com.a0000.digicompass.modules.history.service.HistoryService;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HistoryServiceImpl implements HistoryService {

    private static final int DEFAULT_LIMIT = 20;

    private final HistoryMapper historyMapper;

    public HistoryServiceImpl(HistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    @Override
    public void recordView(Long userId, Long productId) {
        historyMapper.recordView(userId, productId);
    }

    @Override
    public List<ProductListItem> getRecentProducts(Long userId) {
        return historyMapper.findRecentProducts(userId, DEFAULT_LIMIT);
    }

    @Override
    public void clearHistory(Long userId) {
        historyMapper.clearHistory(userId);
    }
}
