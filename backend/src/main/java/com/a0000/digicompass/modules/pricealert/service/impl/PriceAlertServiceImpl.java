package com.a0000.digicompass.modules.pricealert.service.impl;

import com.a0000.digicompass.modules.pricealert.dto.PriceAlertItem;
import com.a0000.digicompass.modules.pricealert.dto.PriceAlertSaveRequest;
import com.a0000.digicompass.modules.pricealert.mapper.PriceAlertMapper;
import com.a0000.digicompass.modules.pricealert.service.PriceAlertService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PriceAlertServiceImpl implements PriceAlertService {

    private final PriceAlertMapper priceAlertMapper;

    public PriceAlertServiceImpl(PriceAlertMapper priceAlertMapper) {
        this.priceAlertMapper = priceAlertMapper;
    }

    @Override
    public List<PriceAlertItem> listAlerts(Long userId) {
        List<PriceAlertItem> alerts = priceAlertMapper.findAllByUser(userId);
        for (PriceAlertItem alert : alerts) {
            refreshAlertStatus(alert);
        }
        return priceAlertMapper.findAllByUser(userId);
    }

    @Override
    public PriceAlertItem getProductAlert(Long userId, Long productId) {
        PriceAlertItem alert = priceAlertMapper.findByUserAndProduct(userId, productId);
        if (alert == null) {
            return null;
        }
        refreshAlertStatus(alert);
        return priceAlertMapper.findByUserAndProduct(userId, productId);
    }

    @Override
    public void saveAlert(Long userId, PriceAlertSaveRequest request) {
        String priceType = normalizePriceType(request.priceType());
        BigDecimal lastPrice = priceAlertMapper.findLatestReferencePrice(request.productId(), priceType);
        String status = determineStatus(lastPrice, request.targetPrice());
        priceAlertMapper.upsertAlert(userId, request.productId(), request.targetPrice(), priceType, lastPrice, status);
    }

    @Override
    public void deleteAlert(Long userId, Long alertId) {
        priceAlertMapper.deleteAlert(userId, alertId);
    }

    @Override
    public List<PriceAlertItem> refreshAll(Long userId) {
        List<PriceAlertItem> alerts = priceAlertMapper.findAllByUser(userId);
        for (PriceAlertItem alert : alerts) {
            refreshAlertStatus(alert);
        }
        return priceAlertMapper.findAllByUser(userId);
    }

    private void refreshAlertStatus(PriceAlertItem alert) {
        BigDecimal lastPrice = priceAlertMapper.findLatestReferencePrice(alert.productId(), alert.priceType());
        if (lastPrice != null) {
            String status = determineStatus(lastPrice, alert.targetPrice());
            priceAlertMapper.updateAlertStatus(alert.id(), lastPrice, status);
        }
    }

    private String determineStatus(BigDecimal currentPrice, BigDecimal targetPrice) {
        if (currentPrice == null) return "ACTIVE";
        return currentPrice.compareTo(targetPrice) <= 0 ? "TRIGGERED" : "ACTIVE";
    }

    private String normalizePriceType(String priceType) {
        return priceType == null || priceType.isBlank() ? "official" : priceType;
    }
}
