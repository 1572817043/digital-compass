package com.a0000.digicompass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductCatalogOptimizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long categoryId;
    private Long gamingProductId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'CatalogTest'");
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('catalog_test_phone', '筛选测试分类', '产品库筛选测试分类', 998, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name), enabled = VALUES(enabled)
                """);
        categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = 'catalog_test_phone'",
                Long.class
        );

        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES ('CatalogTest', 998)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """);
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = 'CatalogTest'",
                Long.class
        );

        gamingProductId = insertProduct(categoryId, brandId, "筛选测试游戏机", "filter-gaming", 4999, 50);
        Long officeProductId = insertProduct(categoryId, brandId, "筛选测试办公机", "filter-office", 8999, 60);

        insertTag(gamingProductId, "scene", "游戏强", "适合游戏用户");
        insertTag(officeProductId, "scene", "办公", "适合办公用户");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'CatalogTest'");
    }

    @Test
    void productsCanFilterByPriceRangeAndTagName() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("minPrice", "4000")
                        .param("maxPrice", "6000")
                        .param("tagName", "游戏强"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("筛选测试游戏机"));
    }

    @Test
    void productsCanSortByPriceAscending() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("sortBy", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("筛选测试游戏机"));
    }

    @Test
    void productsDoNotShowItemsFromDisabledCategories() throws Exception {
        jdbcTemplate.update("UPDATE dc_category SET enabled = 0 WHERE id = ?", categoryId);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void publicCategoriesDoNotExposeBlankRows() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('', '', '脏数据测试分类', 0, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name), enabled = VALUES(enabled)
                """);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == '')]").isEmpty())
                .andExpect(jsonPath("$.data[?(@.name == '')]").isEmpty());
    }

    @Test
    void productDetailReturnsStandardMetrics() throws Exception {
        mockMvc.perform(get("/api/products/{id}", gamingProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.metrics").isArray());
    }

    private Long insertProduct(Long categoryId, Long brandId, String name, String model, int price, int score) {
        jdbcTemplate.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, 'CatalogTest', ?, ?, '筛选测试产品', ?, ?, 1)
                """, categoryId, brandId, name, model, price, score);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_product WHERE category_id = ? AND brand_id = ? AND name = ? AND model = ?",
                Long.class,
                categoryId,
                brandId,
                name,
                model
        );
    }

    private void insertTag(Long productId, String tagType, String tagName, String tagValue) {
        jdbcTemplate.update("""
                INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
                VALUES (?, ?, ?, ?, 10)
                """, productId, tagType, tagName, tagValue);
    }
}
