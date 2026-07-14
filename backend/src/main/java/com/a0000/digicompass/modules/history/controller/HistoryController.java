package com.a0000.digicompass.modules.history.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import com.a0000.digicompass.modules.history.service.HistoryService;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;
    private final AuthService authService;

    public HistoryController(HistoryService historyService, AuthService authService) {
        this.historyService = historyService;
        this.authService = authService;
    }

    @PostMapping("/products/{productId}")
    public ApiResponse<Void> record(@PathVariable Long productId) {
        LoginUser user = authService.currentUser();
        historyService.recordView(user.id(), productId);
        return ApiResponse.success(null);
    }

    @GetMapping("/products")
    public ApiResponse<List<ProductListItem>> list() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(historyService.getRecentProducts(user.id()));
    }

    @DeleteMapping("/products")
    public ApiResponse<Void> clear() {
        LoginUser user = authService.currentUser();
        historyService.clearHistory(user.id());
        return ApiResponse.success(null);
    }
}
