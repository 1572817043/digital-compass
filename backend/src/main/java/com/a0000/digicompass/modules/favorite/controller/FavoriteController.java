package com.a0000.digicompass.modules.favorite.controller;

import com.a0000.digicompass.common.api.ApiResponse;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import com.a0000.digicompass.modules.auth.service.AuthService;
import com.a0000.digicompass.modules.favorite.service.FavoriteService;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final AuthService authService;

    public FavoriteController(FavoriteService favoriteService, AuthService authService) {
        this.favoriteService = favoriteService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<ProductListItem>> list() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(favoriteService.getFavoriteProducts(user.id()));
    }

    @GetMapping("/ids")
    public ApiResponse<List<Long>> ids() {
        LoginUser user = authService.currentUser();
        return ApiResponse.success(favoriteService.getFavoriteIds(user.id()));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Void> add(@PathVariable Long productId) {
        LoginUser user = authService.currentUser();
        favoriteService.addFavorite(user.id(), productId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        LoginUser user = authService.currentUser();
        favoriteService.removeFavorite(user.id(), productId);
        return ApiResponse.success(null);
    }
}
