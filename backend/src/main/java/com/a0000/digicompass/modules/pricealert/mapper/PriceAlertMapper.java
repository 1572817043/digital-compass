package com.a0000.digicompass.modules.pricealert.mapper;

import com.a0000.digicompass.modules.pricealert.dto.PriceAlertItem;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PriceAlertMapper {

    private final JdbcTemplate jdbc;

    public PriceAlertMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public BigDecimal findLatestReferencePrice(Long productId, String priceType) {
        List<BigDecimal> prices = jdbc.queryForList("""
                SELECT COALESCE(avg_price, min_price, max_price) AS ref_price
                FROM dc_price_reference
                WHERE product_id = ? AND price_type = ?
                ORDER BY reference_date DESC, id DESC
                LIMIT 1
                """, BigDecimal.class, productId, priceType
        );
        return prices.isEmpty() ? null : prices.getFirst();
    }

    public List<PriceAlertItem> findAllByUser(Long userId) {
        return jdbc.query("""
                SELECT a.id, a.product_id, p.name AS product_name, b.name AS brand_name,
                       c.name AS category_name, c.code AS category_code, p.cover_url,
                       a.target_price, a.price_type, a.status, a.last_price,
                       CASE WHEN a.last_price IS NOT NULL AND a.last_price <= a.target_price THEN 1 ELSE 0 END AS triggered,
                       DATE_FORMAT(a.triggered_at, '%Y-%m-%d %H:%i:%s') AS triggered_at
                FROM dc_price_alert a
                JOIN dc_product p ON a.product_id = p.id
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE a.user_id = ?
                ORDER BY a.created_at DESC
                """, new Object[]{userId},
                (rs, rowNum) -> new PriceAlertItem(
                        rs.getLong("id"), rs.getLong("product_id"),
                        rs.getString("product_name"), rs.getString("brand_name"),
                        rs.getString("category_name"), rs.getString("category_code"),
                        rs.getString("cover_url"),
                        rs.getBigDecimal("target_price"), rs.getString("price_type"),
                        rs.getString("status"), rs.getBigDecimal("last_price"),
                        rs.getInt("triggered") == 1,
                        rs.getString("triggered_at")
                )
        );
    }

    public PriceAlertItem findByUserAndProduct(Long userId, Long productId) {
        List<PriceAlertItem> results = jdbc.query("""
                SELECT a.id, a.product_id, p.name AS product_name, b.name AS brand_name,
                       c.name AS category_name, c.code AS category_code, p.cover_url,
                       a.target_price, a.price_type, a.status, a.last_price,
                       CASE WHEN a.last_price IS NOT NULL AND a.last_price <= a.target_price THEN 1 ELSE 0 END AS triggered,
                       DATE_FORMAT(a.triggered_at, '%Y-%m-%d %H:%i:%s') AS triggered_at
                FROM dc_price_alert a
                JOIN dc_product p ON a.product_id = p.id
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE a.user_id = ? AND a.product_id = ?
                LIMIT 1
                """, new Object[]{userId, productId},
                (rs, rowNum) -> new PriceAlertItem(
                        rs.getLong("id"), rs.getLong("product_id"),
                        rs.getString("product_name"), rs.getString("brand_name"),
                        rs.getString("category_name"), rs.getString("category_code"),
                        rs.getString("cover_url"),
                        rs.getBigDecimal("target_price"), rs.getString("price_type"),
                        rs.getString("status"), rs.getBigDecimal("last_price"),
                        rs.getInt("triggered") == 1,
                        rs.getString("triggered_at")
                )
        );
        return results.isEmpty() ? null : results.getFirst();
    }

    public void upsertAlert(Long userId, Long productId, BigDecimal targetPrice, String priceType, BigDecimal lastPrice, String status) {
        jdbc.update("""
                INSERT INTO dc_price_alert (user_id, product_id, target_price, price_type, last_price, status)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE target_price = VALUES(target_price), last_price = VALUES(last_price),
                    status = VALUES(status),
                    triggered_at = CASE
                        WHEN VALUES(status) = 'TRIGGERED' AND triggered_at IS NULL THEN NOW()
                        WHEN VALUES(status) = 'ACTIVE' THEN NULL
                        ELSE triggered_at
                    END
                """, userId, productId, targetPrice, priceType, lastPrice, status);
    }

    public void updateAlertStatus(Long alertId, BigDecimal lastPrice, String status) {
        jdbc.update("""
                UPDATE dc_price_alert
                SET last_price = ?,
                    status = ?,
                    triggered_at = CASE
                        WHEN ? = 'TRIGGERED' AND triggered_at IS NULL THEN NOW()
                        WHEN ? = 'ACTIVE' THEN NULL
                        ELSE triggered_at
                    END
                WHERE id = ?
                """, lastPrice, status, status, status, alertId);
    }

    public void deleteAlert(Long userId, Long alertId) {
        jdbc.update("DELETE FROM dc_price_alert WHERE id = ? AND user_id = ?", alertId, userId);
    }
}
