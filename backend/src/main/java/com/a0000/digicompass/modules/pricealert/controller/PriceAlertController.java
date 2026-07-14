package com.a0000.digicompass.modules.pricealert.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import com.a0000.digicompass.modules.pricealert.dto.PriceAlertItem;
import com.a0000.digicompass.modules.pricealert.dto.PriceAlertSaveRequest;
import com.a0000.digicompass.modules.pricealert.service.PriceAlertService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price-alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;
    private final AuthService authService;

    public PriceAlertController(PriceAlertService priceAlertService, AuthService authService) {
        this.priceAlertService = priceAlertService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<PriceAlertItem>> list() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(priceAlertService.listAlerts(user.id()));
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<PriceAlertItem> getProductAlert(@PathVariable Long productId) {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(priceAlertService.getProductAlert(user.id(), productId));
    }

    @PostMapping
    public ApiResponse<Void> save(@Valid @RequestBody PriceAlertSaveRequest request) {
        LoginUser user = authService.currentUser();
        priceAlertService.saveAlert(user.id(), request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        LoginUser user = authService.currentUser();
        priceAlertService.deleteAlert(user.id(), id);
        return ApiResponse.success(null);
    }

    @PostMapping("/refresh")
    public ApiResponse<List<PriceAlertItem>> refresh() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(priceAlertService.refreshAll(user.id()));
    }
}
