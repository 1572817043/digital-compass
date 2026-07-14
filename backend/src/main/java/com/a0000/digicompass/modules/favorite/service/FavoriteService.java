package com.a0000.digicompass.modules.favorite.service;

import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;

public interface FavoriteService {

    List<Long> getFavoriteIds(Long userId);

    List<ProductListItem> getFavoriteProducts(Long userId);

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);
}
