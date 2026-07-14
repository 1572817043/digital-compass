package com.a0000.digicompass.modules.history.mapper;

import com.a0000.digicompass.modules.product.dto.ProductListItem;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HistoryMapper {

    private final JdbcTemplate jdbc;

    public HistoryMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void recordView(Long userId, Long productId) {
        jdbc.update("""
                INSERT INTO dc_user_view_history (user_id, product_id, viewed_at)
                VALUES (?, ?, NOW(3))
                ON DUPLICATE KEY UPDATE viewed_at = NOW(3)
                """, userId, productId);
    }

    public List<ProductListItem> findRecentProducts(Long userId, int limit) {
        return jdbc.query("""
                SELECT p.id, p.name, p.model, p.summary, p.official_price, p.score,
                       p.category_id, c.name AS category_name, c.code AS category_code,
                       p.brand_id, b.name AS brand_name, p.cover_url
                FROM dc_user_view_history h
                JOIN dc_product p ON h.product_id = p.id
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE h.user_id = ?
                ORDER BY h.viewed_at DESC, h.id DESC
                LIMIT ?
                """, new Object[]{userId, limit},
                (rs, rowNum) -> new ProductListItem(
                        rs.getLong("id"), rs.getString("name"), rs.getString("model"),
                        rs.getString("summary"), rs.getBigDecimal("official_price"), rs.getInt("score"),
                        rs.getLong("category_id"), rs.getString("category_name"), rs.getString("category_code"),
                        rs.getLong("brand_id"), rs.getString("brand_name"), rs.getString("cover_url")
                )
        );
    }

    public void clearHistory(Long userId) {
        jdbc.update("DELETE FROM dc_user_view_history WHERE user_id = ?", userId);
    }
}
