package com.a0000.digicompass.modules.favorite.mapper;

import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteMapper {

    private final JdbcTemplate jdbc;

    public FavoriteMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Long> findFavoriteIds(Long userId) {
        return jdbc.queryForList(
                "SELECT product_id FROM dc_user_favorite WHERE user_id = ? ORDER BY created_at DESC",
                Long.class, userId
        );
    }

    public List<ProductListItem> findFavoriteProducts(Long userId) {
        return jdbc.query("""
                SELECT p.id, p.name, p.model, p.summary, p.official_price, p.score,
                       p.category_id, c.name AS category_name, c.code AS category_code,
                       p.brand_id, b.name AS brand_name, p.cover_url
                FROM dc_user_favorite f
                JOIN dc_product p ON f.product_id = p.id
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE f.user_id = ?
                ORDER BY f.created_at DESC
                """, new Object[]{userId},
                (rs, rowNum) -> new ProductListItem(
                        rs.getLong("id"), rs.getString("name"), rs.getString("model"),
                        rs.getString("summary"), rs.getBigDecimal("official_price"), rs.getInt("score"),
                        rs.getLong("category_id"), rs.getString("category_name"), rs.getString("category_code"),
                        rs.getLong("brand_id"), rs.getString("brand_name"), rs.getString("cover_url")
                )
        );
    }

    public void addFavorite(Long userId, Long productId) {
        jdbc.update(
                "INSERT IGNORE INTO dc_user_favorite (user_id, product_id) VALUES (?, ?)",
                userId, productId
        );
    }

    public void removeFavorite(Long userId, Long productId) {
        jdbc.update(
                "DELETE FROM dc_user_favorite WHERE user_id = ? AND product_id = ?",
                userId, productId
        );
    }
}
