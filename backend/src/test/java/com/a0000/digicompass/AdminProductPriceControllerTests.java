package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProductPriceControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
                "Admin Price Test Product",
                "admin-price-test-model"
        );
        jdbcTemplate.update(
                "DELETE FROM dc_product WHERE name = ? AND model = ?",
                "Admin Price Test Product",
                "admin-price-test-model"
        );
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "AdminPriceTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "admin-price-test");
    }

    @Test
    void adminPriceEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/products/{productId}/prices", 1L))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/products/{productId}/prices", 1L)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanMaintainCompletePriceReferenceFields() throws Exception {
        Long productId = seedProduct();
        String token = adminToken();

        mockMvc.perform(post("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priceType":"channel",
                                  "platform":"渠道测试",
                                  "minPrice":4800,
                                  "maxPrice":5200,
                                  "avgPrice":5000,
                                  "sampleCount":3,
                                  "referenceDate":"2026-07-12",
                                  "sourceType":"manual",
                                  "remark":"初始备注"
                                }
                                """))
                .andExpect(status().isOk());

        Long priceId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_price_reference WHERE product_id = ? AND price_type = ? AND platform = ?",
                Long.class,
                productId,
                "channel",
                "渠道测试"
        );

        mockMvc.perform(get("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].priceType").value("channel"))
                .andExpect(jsonPath("$.data[0].platform").value("渠道测试"))
                .andExpect(jsonPath("$.data[0].sampleCount").value(3))
                .andExpect(jsonPath("$.data[0].referenceDate").value("2026-07-12"))
                .andExpect(jsonPath("$.data[0].sourceType").value("manual"))
                .andExpect(jsonPath("$.data[0].remark").value("初始备注"));

        mockMvc.perform(put("/api/admin/products/{productId}/prices/{priceId}", productId, priceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priceType":"used",
                                  "platform":"二手测试",
                                  "minPrice":4300,
                                  "maxPrice":4700,
                                  "avgPrice":4500,
                                  "sampleCount":5,
                                  "referenceDate":"2026-07-13",
                                  "sourceType":"manual",
                                  "remark":"更新备注"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].priceType").value("used"))
                .andExpect(jsonPath("$.data[0].platform").value("二手测试"))
                .andExpect(jsonPath("$.data[0].sampleCount").value(5))
                .andExpect(jsonPath("$.data[0].referenceDate").value("2026-07-13"))
                .andExpect(jsonPath("$.data[0].remark").value("更新备注"));

        mockMvc.perform(delete("/api/admin/products/{productId}/prices/{priceId}", productId, priceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_price_reference WHERE id = ?",
                Integer.class,
                priceId
        );
        assertThat(remaining).isZero();
    }

    @Test
    void adminPriceRejectsInvalidPayloads() throws Exception {
        Long productId = seedProduct();
        String token = adminToken();

        mockMvc.perform(post("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priceType":"bad","platform":"测试","avgPrice":5000,"referenceDate":"2026-07-12"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priceType":"used","platform":"测试","referenceDate":"2026-07-12"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priceType":"used","platform":"测试","minPrice":6000,"maxPrice":5000,"referenceDate":"2026-07-12"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"priceType":"used","platform":"测试","avgPrice":5000}
                                """))
                .andExpect(status().isBadRequest());
    }

    private Long seedProduct() {
        Long categoryId = seedCategory();
        Long brandId = seedBrand();
        jdbcTemplate.update(
                """
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  category_id = VALUES(category_id),
                  brand_id = VALUES(brand_id),
                  brand = VALUES(brand),
                  summary = VALUES(summary),
                  official_price = VALUES(official_price),
                  score = VALUES(score),
                  status = VALUES(status)
                """,
                categoryId,
                brandId,
                "AdminPriceTestBrand",
                "Admin Price Test Product",
                "admin-price-test-model",
                "管理端价格测试产品",
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
                "Admin Price Test Product",
                "admin-price-test-model"
        );
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
                "admin-price-test",
                "管理端价格测试分类",
                "管理端价格接口测试使用",
                995,
                1
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_category WHERE code = ?", Long.class, "admin-price-test");
    }

    private Long seedBrand() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "AdminPriceTestBrand",
                995
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_brand WHERE name = ?", Long.class, "AdminPriceTestBrand");
    }

    private String adminToken() {
        return jwtTokenProvider.createToken(new LoginUser(9001L, "admin_price_admin", "管理端价格测试管理员", "ADMIN"));
    }

    private String userToken() {
        return jwtTokenProvider.createToken(new LoginUser(9002L, "admin_price_user", "管理端价格测试用户", "USER"));
    }
}
