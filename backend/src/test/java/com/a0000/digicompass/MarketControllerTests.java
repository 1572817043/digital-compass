package com.a0000.digicompass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MarketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanTestData() {
        jdbcTemplate.update(
                """
                DELETE FROM dc_price_reference
                WHERE product_id IN (
                    SELECT id FROM dc_product
                    WHERE name = ? AND model = ?
                )
                """,
                "Market Test Product",
                "market-test-model"
        );
        jdbcTemplate.update(
                "DELETE FROM dc_product WHERE name = ? AND model = ?",
                "Market Test Product",
                "market-test-model"
        );
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "MarketTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "market-test");
    }

    @Test
    void marketPricesReturnLatestReferencePerProductAndPriceType() throws Exception {
        Long categoryId = seedCategory();
        Long brandId = seedBrand();
        Long productId = seedProduct(categoryId, brandId);
        seedReferencePrice(productId, "official", "MarketTestOld", new BigDecimal("8000.00"), LocalDate.now().minusDays(10));
        seedReferencePrice(productId, "official", "MarketTestLatest", new BigDecimal("6000.00"), LocalDate.now());
        seedReferencePrice(productId, "used", "MarketTestUsed", new BigDecimal("5000.00"), LocalDate.now());

        mockMvc.perform(get("/api/market/prices")
                        .param("keyword", "Market Test Product")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("brandId", String.valueOf(brandId))
                        .param("priceType", "official"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].productId").value(productId.intValue()))
                .andExpect(jsonPath("$.data[0].priceType").value("official"))
                .andExpect(jsonPath("$.data[0].avgPrice").value(6000.00));
    }

    @Test
    void marketProductPricesReturnProductPriceHistory() throws Exception {
        Long categoryId = seedCategory();
        Long brandId = seedBrand();
        Long productId = seedProduct(categoryId, brandId);
        seedReferencePrice(productId, "official", "MarketTestOld", new BigDecimal("8000.00"), LocalDate.now().minusDays(10));
        seedReferencePrice(productId, "official", "MarketTestLatest", new BigDecimal("6000.00"), LocalDate.now());
        seedReferencePrice(productId, "used", "MarketTestUsed", new BigDecimal("5000.00"), LocalDate.now());

        mockMvc.perform(get("/api/market/products/{productId}/prices", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].productId").value(productId.intValue()));
    }

    @Test
    void marketSummaryIsPublic() throws Exception {
        mockMvc.perform(get("/api/market/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProducts").isNumber())
                .andExpect(jsonPath("$.data.productsWithPrice").isNumber());
    }

    private Long seedCategory() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  name = VALUES(name),
                  description = VALUES(description),
                  enabled = VALUES(enabled)
                """,
                "market-test",
                "行情测试分类",
                "行情接口测试使用",
                996,
                1
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_category WHERE code = ?", Long.class, "market-test");
    }

    private Long seedBrand() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "MarketTestBrand",
                996
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_brand WHERE name = ?", Long.class, "MarketTestBrand");
    }

    private Long seedProduct(Long categoryId, Long brandId) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  summary = VALUES(summary),
                  official_price = VALUES(official_price),
                  score = VALUES(score),
                  status = VALUES(status)
                """,
                categoryId,
                brandId,
                "MarketTestBrand",
                "Market Test Product",
                "market-test-model",
                "行情接口测试产品",
                6999,
                82,
                1
        );
        return jdbcTemplate.queryForObject(
                """
                SELECT id FROM dc_product
                WHERE category_id = ? AND brand_id = ? AND name = ? AND model = ?
                """,
                Long.class,
                categoryId,
                brandId,
                "Market Test Product",
                "market-test-model"
        );
    }

    private void seedReferencePrice(Long productId, String priceType, String platform, BigDecimal avgPrice, LocalDate referenceDate) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  min_price = VALUES(min_price),
                  max_price = VALUES(max_price),
                  avg_price = VALUES(avg_price),
                  sample_count = VALUES(sample_count),
                  source_type = VALUES(source_type),
                  remark = VALUES(remark)
                """,
                productId,
                priceType,
                platform,
                avgPrice,
                avgPrice,
                avgPrice,
                1,
                referenceDate,
                "manual",
                "行情接口测试价格"
        );
    }
}
