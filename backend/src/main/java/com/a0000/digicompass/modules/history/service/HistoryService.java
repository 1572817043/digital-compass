package com.a0000.digicompass.modules.history.service;

import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;

public interface HistoryService {

    void recordView(Long userId, Long productId);

    List<ProductListItem> getRecentProducts(Long userId);

    void clearHistory(Long userId);
}
