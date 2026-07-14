package com.a0000.digicompass.modules.favorite.service.impl;

import com.a0000.digicompass.modules.favorite.mapper.FavoriteMapper;
import com.a0000.digicompass.modules.favorite.service.FavoriteService;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public List<Long> getFavoriteIds(Long userId) {
        return favoriteMapper.findFavoriteIds(userId);
    }

    @Override
    public List<ProductListItem> getFavoriteProducts(Long userId) {
        return favoriteMapper.findFavoriteProducts(userId);
    }

    @Override
    public void addFavorite(Long userId, Long productId) {
        favoriteMapper.addFavorite(userId, productId);
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        favoriteMapper.removeFavorite(userId, productId);
    }
}
