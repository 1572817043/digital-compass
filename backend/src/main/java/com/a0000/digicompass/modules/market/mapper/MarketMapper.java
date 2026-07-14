package com.a0000.digicompass.modules.market.mapper;

import com.a0000.digicompass.modules.market.dto.MarketPriceItem;
import com.a0000.digicompass.modules.market.dto.MarketProductPriceRecord;
import com.a0000.digicompass.modules.market.dto.MarketSummary;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketMapper {

    private final JdbcTemplate jdbc;

    public MarketMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MarketPriceItem> findPrices(Long categoryId, Long brandId, String keyword, String priceType, String sort) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id AS product_id, p.name AS product_name, b.name AS brand_name,
                       c.name AS category_name, c.code AS category_code, p.cover_url,
                       p.official_price, p.score,
                       pr.price_type, pr.min_price, pr.max_price, pr.avg_price,
                       pr.sample_count, pr.reference_date
                FROM (
                    SELECT source.*,
                           ROW_NUMBER() OVER (
                               PARTITION BY source.product_id, source.price_type
                               ORDER BY source.reference_date DESC, source.id DESC
                           ) AS row_num
                    FROM dc_price_reference source
                ) pr
                JOIN dc_product p ON pr.product_id = p.id
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE p.status = 1 AND pr.row_num = 1
                """);
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }
        if (brandId != null) {
            sql.append(" AND p.brand_id = ?");
            params.add(brandId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.name LIKE ? OR b.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (priceType != null && !priceType.isBlank() && !"all".equals(priceType)) {
            sql.append(" AND pr.price_type = ?");
            params.add(priceType);
        }

        switch (sort != null ? sort : "latest") {
            case "priceAsc" -> sql.append(" ORDER BY COALESCE(pr.avg_price, pr.min_price) ASC");
            case "priceDesc" -> sql.append(" ORDER BY COALESCE(pr.avg_price, pr.max_price) DESC");
            case "scoreDesc" -> sql.append(" ORDER BY p.score DESC, pr.reference_date DESC");
            default -> sql.append(" ORDER BY pr.reference_date DESC, p.score DESC");
        }

        return jdbc.query(sql.toString(), params.toArray(), (rs, rowNum) -> new MarketPriceItem(
                rs.getLong("product_id"), rs.getString("product_name"), rs.getString("brand_name"),
                rs.getString("category_name"), rs.getString("category_code"), rs.getString("cover_url"),
                rs.getBigDecimal("official_price"), rs.getInt("score"),
                rs.getString("price_type"), rs.getBigDecimal("min_price"),
                rs.getBigDecimal("max_price"), rs.getBigDecimal("avg_price"),
                rs.getInt("sample_count"), rs.getDate("reference_date") != null ?
                        rs.getDate("reference_date").toLocalDate() : null
        ));
    }

    public List<MarketProductPriceRecord> findProductPrices(Long productId) {
        return jdbc.query("""
                SELECT pr.product_id, pr.price_type, pr.platform, pr.min_price, pr.max_price,
                       pr.avg_price, pr.sample_count, pr.reference_date, pr.source_type, pr.remark
                FROM dc_price_reference pr
                JOIN dc_product p ON pr.product_id = p.id
                WHERE pr.product_id = ? AND p.status = 1
                ORDER BY pr.reference_date DESC, pr.id DESC
                """, (rs, rowNum) -> new MarketProductPriceRecord(
                rs.getLong("product_id"),
                rs.getString("price_type"),
                rs.getString("platform"),
                rs.getBigDecimal("min_price"),
                rs.getBigDecimal("max_price"),
                rs.getBigDecimal("avg_price"),
                rs.getInt("sample_count"),
                rs.getDate("reference_date") != null ? rs.getDate("reference_date").toLocalDate() : null,
                rs.getString("source_type"),
                rs.getString("remark")
        ), productId);
    }

    public MarketSummary getSummary() {
        int totalProducts = jdbc.queryForObject("SELECT COUNT(*) FROM dc_product WHERE status = 1", Integer.class);
        int productsWithPrice = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT pr.product_id)
                FROM dc_price_reference pr
                JOIN dc_product p ON pr.product_id = p.id
                WHERE p.status = 1
                """, Integer.class);
        int usedPriceCount = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT pr.product_id)
                FROM dc_price_reference pr
                JOIN dc_product p ON pr.product_id = p.id
                WHERE p.status = 1 AND pr.price_type = 'used'
                """, Integer.class);
        int recentUpdates = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM dc_price_reference pr
                JOIN dc_product p ON pr.product_id = p.id
                WHERE p.status = 1 AND pr.reference_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                """, Integer.class);
        return new MarketSummary(totalProducts, productsWithPrice, usedPriceCount, recentUpdates);
    }
}
