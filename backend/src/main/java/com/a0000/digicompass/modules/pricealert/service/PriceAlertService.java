package com.a0000.digicompass.modules.pricealert.service;

import com.a0000.digicompass.modules.pricealert.dto.PriceAlertItem;
import com.a0000.digicompass.modules.pricealert.dto.PriceAlertSaveRequest;
import java.util.List;

public interface PriceAlertService {

    List<PriceAlertItem> listAlerts(Long userId);

    PriceAlertItem getProductAlert(Long userId, Long productId);

    void saveAlert(Long userId, PriceAlertSaveRequest request);

    void deleteAlert(Long userId, Long alertId);

    List<PriceAlertItem> refreshAll(Long userId);
}
