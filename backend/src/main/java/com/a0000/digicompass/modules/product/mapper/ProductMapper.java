package com.a0000.digicompass.modules.product.mapper;

import com.a0000.digicompass.modules.product.dto.ProductDetail;
import com.a0000.digicompass.modules.product.dto.ProductListItem;
import com.a0000.digicompass.modules.product.dto.ProductMetricItem;
import com.a0000.digicompass.modules.product.dto.ProductTagItem;
import com.a0000.digicompass.modules.product.entity.Brand;
import com.a0000.digicompass.modules.product.entity.Category;
import com.a0000.digicompass.modules.product.entity.ProductImage;
import com.a0000.digicompass.modules.product.entity.ProductSpec;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductMapper {

    private final JdbcTemplate jdbc;

    public ProductMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductListItem> findProducts(
            Long categoryId,
            Long brandId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String tagType,
            String tagName,
            Boolean hasUsedPrice,
            Boolean hasPurchaseLink,
            String sortBy
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.name, p.model, p.summary, p.official_price, p.score,
                       p.category_id, c.name AS category_name, c.code AS category_code,
                       p.brand_id, b.name AS brand_name,
                       COALESCE(p.cover_url, (SELECT pi.image_url FROM dc_product_image pi WHERE pi.product_id = p.id AND pi.image_type = 'MAIN' LIMIT 1)) AS cover_url
                FROM dc_product p
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE p.status = 1 AND c.enabled = 1 AND c.code <> '' AND c.name <> ''
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
            sql.append(" AND (p.name LIKE ? OR p.model LIKE ? OR b.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (minPrice != null) {
            sql.append(" AND p.official_price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND p.official_price <= ?");
            params.add(maxPrice);
        }
        if (tagType != null && !tagType.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM dc_product_tag pt WHERE pt.product_id = p.id AND pt.tag_type = ?)");
            params.add(tagType.trim());
        }
        if (tagName != null && !tagName.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM dc_product_tag pt WHERE pt.product_id = p.id AND pt.tag_name = ?)");
            params.add(tagName.trim());
        }
        if (Boolean.TRUE.equals(hasUsedPrice)) {
            sql.append(" AND EXISTS (SELECT 1 FROM dc_price_reference pr WHERE pr.product_id = p.id AND pr.price_type = 'used')");
        }
        if (Boolean.TRUE.equals(hasPurchaseLink)) {
            sql.append(" AND EXISTS (SELECT 1 FROM dc_purchase_link pl WHERE pl.product_id = p.id AND pl.enabled = 1)");
        }
        sql.append(orderByClause(sortBy));

        return jdbc.query(sql.toString(), params.toArray(), (rs, rowNum) -> new ProductListItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("model"),
                rs.getString("summary"),
                rs.getBigDecimal("official_price"),
                rs.getInt("score"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("category_code"),
                rs.getLong("brand_id"),
                rs.getString("brand_name"),
                rs.getString("cover_url")
        ));
    }

    private String orderByClause(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return " ORDER BY p.score DESC, p.id DESC";
        }
        return switch (sortBy.trim()) {
            case "price_asc" -> " ORDER BY p.official_price IS NULL, p.official_price ASC, p.score DESC, p.id DESC";
            case "price_desc" -> " ORDER BY p.official_price IS NULL, p.official_price DESC, p.score DESC, p.id DESC";
            case "score_asc" -> " ORDER BY p.score ASC, p.id DESC";
            case "newest" -> " ORDER BY p.release_date IS NULL, p.release_date DESC, p.id DESC";
            default -> " ORDER BY p.score DESC, p.id DESC";
        };
    }

    public ProductDetail findDetail(Long productId) {
        String sql = """
                SELECT p.id, p.name, p.model, p.summary, p.official_price, p.score, p.status,
                       p.category_id, c.name AS category_name, c.code AS category_code,
                       p.brand_id, b.name AS brand_name
                FROM dc_product p
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE p.id = ?
                """;
        List<ProductDetail> results = jdbc.query(sql, new Object[]{productId}, (rs, rowNum) -> new ProductDetail(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("model"),
                rs.getString("summary"),
                rs.getBigDecimal("official_price"),
                rs.getInt("score"),
                rs.getInt("status"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getString("category_code"),
                rs.getLong("brand_id"),
                rs.getString("brand_name"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        ));
        if (results.isEmpty()) return null;
        ProductDetail base = results.getFirst();

        List<ProductImage> images = jdbc.query(
                "SELECT id, product_id, image_url, image_type, sort_order FROM dc_product_image WHERE product_id = ? ORDER BY sort_order",
                new Object[]{productId},
                (rs, rn) -> new ProductImage(rs.getLong("id"), rs.getLong("product_id"), rs.getString("image_url"), rs.getString("image_type"), rs.getInt("sort_order"))
        );

        List<ProductSpec> specs = jdbc.query(
                "SELECT spec_group, spec_name, spec_value FROM dc_product_spec WHERE product_id = ? ORDER BY sort_order",
                new Object[]{productId},
                (rs, rn) -> new ProductSpec(rs.getString("spec_group"), rs.getString("spec_name"), rs.getString("spec_value"))
        );

        List<ProductMetricItem> metrics = findMetrics(productId);
        List<ProductTagItem> tags = findTags(productId);

        List<ProductDetail.PriceInfo> prices = jdbc.query(
                """
                SELECT id, price_type, platform, min_price, max_price, avg_price,
                       sample_count, reference_date, source_type, remark
                FROM dc_price_reference
                WHERE product_id = ?
                ORDER BY reference_date DESC, id DESC
                """,
                new Object[]{productId},
                (rs, rn) -> mapPriceInfo(rs)
        );

        List<ProductDetail.PurchaseLinkInfo> links = jdbc.query(
                "SELECT id, platform, link_type, title, url, sort_order FROM dc_purchase_link WHERE product_id = ? AND enabled = 1 ORDER BY sort_order",
                new Object[]{productId},
                (rs, rn) -> new ProductDetail.PurchaseLinkInfo(rs.getLong("id"), rs.getString("platform"), rs.getString("link_type"), rs.getString("title"), rs.getString("url"), rs.getInt("sort_order"))
        );

        return new ProductDetail(
                base.id(), base.name(), base.model(), base.summary(), base.officialPrice(), base.score(), base.status(),
                base.categoryId(), base.categoryName(), base.categoryCode(),
                base.brandId(), base.brandName(),
                images, specs, metrics, tags, prices, links
        );
    }

    public java.util.List<ProductMetricItem> findMetrics(Long productId) {
        return jdbc.query(
                """
                SELECT id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order
                FROM dc_product_metric
                WHERE product_id = ?
                ORDER BY sort_order, id
                """,
                new Object[]{productId},
                (rs, rn) -> new ProductMetricItem(
                        rs.getLong("id"),
                        rs.getString("metric_key"),
                        rs.getString("metric_label"),
                        rs.getString("metric_value"),
                        rs.getBigDecimal("numeric_value"),
                        rs.getString("unit"),
                        rs.getInt("sort_order")
                )
        );
    }

    public List<Brand> findAllBrands() {
        return jdbc.query("SELECT id, name, logo_url, sort_order FROM dc_brand ORDER BY sort_order",
                (rs, rn) -> new Brand(rs.getLong("id"), rs.getString("name"), rs.getString("logo_url"), rs.getInt("sort_order")));
    }

    public List<Category> findAllCategories() {
        return jdbc.query("SELECT id, code, name, description FROM dc_category WHERE enabled = 1 AND code <> '' AND name <> '' ORDER BY sort_order",
                (rs, rn) -> new Category(rs.getLong("id"), rs.getString("code"), rs.getString("name"), rs.getString("description")));
    }

    public Long insertProduct(Long categoryId, Long brandId, String name, String model, String summary, String coverUrl, java.math.BigDecimal officialPrice, Integer score, Integer status) {
        String brandName = jdbc.queryForObject("SELECT name FROM dc_brand WHERE id = ?", new Object[]{brandId}, String.class);
        jdbc.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, cover_url, official_price, score, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, categoryId, brandId, brandName, name, model, summary, coverUrl, officialPrice, score != null ? score : 0, status != null ? status : 1);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateProduct(Long id, Long categoryId, Long brandId, String name, String model, String summary, String coverUrl, java.math.BigDecimal officialPrice, Integer score, Integer status) {
        String brandName = jdbc.queryForObject("SELECT name FROM dc_brand WHERE id = ?", new Object[]{brandId}, String.class);
        jdbc.update("""
                UPDATE dc_product SET category_id=?, brand_id=?, brand=?, name=?, model=?, summary=?, cover_url=?, official_price=?, score=?, status=? WHERE id=?
                """, categoryId, brandId, brandName, name, model, summary, coverUrl, officialPrice, score, status, id);
    }

    public void deleteProduct(Long id) {
        jdbc.update("DELETE FROM dc_product WHERE id = ?", id);
    }

    public List<ProductListItem> findAllProducts(Integer status) {
        StringBuilder sql = new StringBuilder("""
                SELECT p.id, p.name, p.model, p.summary, p.official_price, p.score, p.status,
                       p.category_id, c.name AS category_name, c.code AS category_code,
                       p.brand_id, b.name AS brand_name,
                       COALESCE(p.cover_url, (SELECT pi.image_url FROM dc_product_image pi WHERE pi.product_id = p.id AND pi.image_type = 'MAIN' LIMIT 1)) AS cover_url
                FROM dc_product p
                JOIN dc_category c ON p.category_id = c.id
                LEFT JOIN dc_brand b ON p.brand_id = b.id
                WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY p.id DESC");
        return jdbc.query(sql.toString(), params.toArray(), (rs, rowNum) -> new ProductListItem(
                rs.getLong("id"), rs.getString("name"), rs.getString("model"),
                rs.getString("summary"), rs.getBigDecimal("official_price"), rs.getInt("score"),
                rs.getLong("category_id"), rs.getString("category_name"), rs.getString("category_code"),
                rs.getLong("brand_id"), rs.getString("brand_name"), rs.getString("cover_url")
        ));
    }

    public void updateStatus(Long id, int status) {
        jdbc.update("UPDATE dc_product SET status = ? WHERE id = ?", status, id);
    }

    public void insertProductImage(Long productId, String imageUrl, String imageType, int sortOrder) {
        String type = imageType != null && !imageType.isBlank() ? imageType.trim().toUpperCase() : "GALLERY";
        if ("MAIN".equals(type)) {
            jdbc.update("DELETE FROM dc_product_image WHERE product_id = ? AND image_type = 'MAIN'", productId);
        }
        jdbc.update("INSERT INTO dc_product_image (product_id, image_url, image_type, sort_order) VALUES (?, ?, ?, ?)",
                productId, imageUrl, type, sortOrder);
        if ("MAIN".equals(type)) {
            jdbc.update("UPDATE dc_product SET cover_url = ? WHERE id = ?", imageUrl, productId);
        }
    }

    public void deleteProductImage(Long imageId) {
        jdbc.update("DELETE FROM dc_product_image WHERE id = ?", imageId);
    }

    public int countProducts() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM dc_product", Integer.class);
    }

    public int countCategories() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM dc_category", Integer.class);
    }

    // ========== Specs ==========

    public java.util.List<ProductSpec> findSpecs(Long productId) {
        return jdbc.query(
                "SELECT id, product_id, spec_group, spec_name, spec_value, sort_order FROM dc_product_spec WHERE product_id = ? ORDER BY sort_order, id",
                new Object[]{productId},
                (rs, rn) -> new ProductSpec(rs.getLong("id"), rs.getString("spec_group"), rs.getString("spec_name"), rs.getString("spec_value"), rs.getInt("sort_order"))
        );
    }

    public Long insertSpec(Long productId, String specGroup, String specName, String specValue, int sortOrder) {
        jdbc.update("INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order) VALUES (?, ?, ?, ?, ?)",
                productId, specGroup, specName, specValue, sortOrder);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateSpec(Long specId, Long productId, String specGroup, String specName, String specValue, int sortOrder) {
        jdbc.update("UPDATE dc_product_spec SET spec_group=?, spec_name=?, spec_value=?, sort_order=? WHERE id=? AND product_id=?",
                specGroup, specName, specValue, sortOrder, specId, productId);
    }

    public void deleteSpec(Long specId, Long productId) {
        jdbc.update("DELETE FROM dc_product_spec WHERE id = ? AND product_id = ?", specId, productId);
    }

    // ========== Prices ==========

    public java.util.List<ProductDetail.PriceInfo> findPrices(Long productId) {
        return jdbc.query(
                """
                SELECT id, price_type, platform, min_price, max_price, avg_price,
                       sample_count, reference_date, source_type, remark
                FROM dc_price_reference
                WHERE product_id = ?
                ORDER BY reference_date DESC, id DESC
                """,
                new Object[]{productId},
                (rs, rn) -> mapPriceInfo(rs)
        );
    }

    private ProductDetail.PriceInfo mapPriceInfo(ResultSet rs) throws SQLException {
        return new ProductDetail.PriceInfo(
                rs.getLong("id"),
                rs.getString("price_type"),
                rs.getString("platform"),
                rs.getBigDecimal("min_price"),
                rs.getBigDecimal("max_price"),
                rs.getBigDecimal("avg_price"),
                rs.getInt("sample_count"),
                rs.getObject("reference_date", java.time.LocalDate.class),
                rs.getString("source_type"),
                rs.getString("remark")
        );
    }

    public Long insertPrice(Long productId, String priceType, String platform, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, java.math.BigDecimal avgPrice, Integer sampleCount, java.time.LocalDate referenceDate, String sourceType, String remark) {
        jdbc.update("INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                productId, priceType, platform, minPrice, maxPrice, avgPrice, sampleCount != null ? sampleCount : 0, referenceDate, sourceType != null ? sourceType : "manual", remark);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updatePrice(Long priceId, Long productId, String priceType, String platform, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, java.math.BigDecimal avgPrice, Integer sampleCount, java.time.LocalDate referenceDate, String sourceType, String remark) {
        jdbc.update("UPDATE dc_price_reference SET price_type=?, platform=?, min_price=?, max_price=?, avg_price=?, sample_count=?, reference_date=?, source_type=?, remark=? WHERE id=? AND product_id=?",
                priceType, platform, minPrice, maxPrice, avgPrice, sampleCount != null ? sampleCount : 0, referenceDate, sourceType != null ? sourceType : "manual", remark, priceId, productId);
    }

    public void deletePrice(Long priceId, Long productId) {
        jdbc.update("DELETE FROM dc_price_reference WHERE id = ? AND product_id = ?", priceId, productId);
    }

    // ========== Links ==========

    public java.util.List<ProductDetail.PurchaseLinkInfo> findLinks(Long productId) {
        return jdbc.query(
                "SELECT id, platform, link_type, title, url, sort_order FROM dc_purchase_link WHERE product_id = ? ORDER BY sort_order, id",
                new Object[]{productId},
                (rs, rn) -> new ProductDetail.PurchaseLinkInfo(rs.getLong("id"), rs.getString("platform"), rs.getString("link_type"), rs.getString("title"), rs.getString("url"), rs.getInt("sort_order"))
        );
    }

    public Long insertLink(Long productId, String platform, String linkType, String title, String url, int sortOrder, int enabled) {
        jdbc.update("INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)",
                productId, platform, linkType != null ? linkType : "official", title, url, sortOrder, enabled);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateLink(Long linkId, Long productId, String platform, String linkType, String title, String url, int sortOrder, int enabled) {
        jdbc.update("UPDATE dc_purchase_link SET platform=?, link_type=?, title=?, url=?, sort_order=?, enabled=? WHERE id=? AND product_id=?",
                platform, linkType, title, url, sortOrder, enabled, linkId, productId);
    }

    public void deleteLink(Long linkId, Long productId) {
        jdbc.update("DELETE FROM dc_purchase_link WHERE id = ? AND product_id = ?", linkId, productId);
    }

    // ========== Tags ==========

    public java.util.List<ProductTagItem> findTags(Long productId) {
        return jdbc.query(
                """
                SELECT id, tag_type, tag_name, tag_value, sort_order
                FROM dc_product_tag
                WHERE product_id = ?
                ORDER BY FIELD(tag_type, 'selling_point', 'weakness', 'suitable', 'unsuitable', 'scene'), sort_order, id
                """,
                new Object[]{productId},
                (rs, rn) -> new ProductTagItem(
                        rs.getLong("id"),
                        rs.getString("tag_type"),
                        rs.getString("tag_name"),
                        rs.getString("tag_value"),
                        rs.getInt("sort_order")
                )
        );
    }

    public Long insertTag(Long productId, String tagType, String tagName, String tagValue, int sortOrder) {
        jdbc.update("""
                INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
                VALUES (?, ?, ?, ?, ?)
                """, productId, tagType, tagName, tagValue, sortOrder);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void updateTag(Long tagId, Long productId, String tagType, String tagName, String tagValue, int sortOrder) {
        jdbc.update("""
                UPDATE dc_product_tag
                SET tag_type = ?, tag_name = ?, tag_value = ?, sort_order = ?
                WHERE id = ? AND product_id = ?
                """, tagType, tagName, tagValue, sortOrder, tagId, productId);
    }

    public void deleteTag(Long tagId, Long productId) {
        jdbc.update("DELETE FROM dc_product_tag WHERE id = ? AND product_id = ?", tagId, productId);
    }
}
