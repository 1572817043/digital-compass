package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import java.math.BigDecimal;
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
class PriceAlertControllerTests {

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
                DELETE FROM dc_price_alert
                WHERE user_id IN (SELECT id FROM dc_user WHERE username IN (?, ?))
                   OR product_id IN (
                       SELECT id FROM dc_product
                       WHERE name = ? AND model = ?
                   )
                """,
                "price_alert_test_user",
                "price_alert_other_user",
                "Price Alert Test Product",
                "price-alert-test-model"
        );
        jdbcTemplate.update(
                """
                DELETE FROM dc_price_reference
                WHERE product_id IN (
                    SELECT id FROM dc_product
                    WHERE name = ? AND model = ?
                )
                """,
                "Price Alert Test Product",
                "price-alert-test-model"
        );
        jdbcTemplate.update(
                "DELETE FROM dc_product WHERE name = ? AND model = ?",
                "Price Alert Test Product",
                "price-alert-test-model"
        );
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "PriceAlertTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "price-alert-test");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username IN (?, ?)", "price_alert_test_user", "price_alert_other_user");
    }

    @Test
    void priceAlertEndpointsRequireJwt() throws Exception {
        mockMvc.perform(get("/api/price-alerts"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/price-alerts/product/{productId}", 1L))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/price-alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":1,"targetPrice":4999,"priceType":"official"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/price-alerts/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void priceAlertFlowCanCreateRefreshTriggerListAndDelete() throws Exception {
        Long userId = seedUser("price_alert_test_user", "价格提醒测试用户");
        Long productId = seedProduct();
        seedReferencePrice(productId, new BigDecimal("6000.00"));
        jdbcTemplate.update("DELETE FROM dc_price_alert WHERE user_id = ? AND product_id = ?", userId, productId);

        String token = jwtTokenProvider.createToken(new LoginUser(userId, "price_alert_test_user", "价格提醒测试用户", "USER"));

        mockMvc.perform(post("/api/price-alerts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"targetPrice":5000,"priceType":"official"}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        Integer alertCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
        );
        assertThat(alertCount).isEqualTo(1);

        mockMvc.perform(get("/api/price-alerts/product/{productId}", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId.intValue()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.lastPrice").value(6000.00))
                .andExpect(jsonPath("$.data.triggered").value(false));

        seedReferencePrice(productId, new BigDecimal("4000.00"));

        mockMvc.perform(get("/api/price-alerts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(productId.intValue()))
                .andExpect(jsonPath("$.data[0].status").value("TRIGGERED"))
                .andExpect(jsonPath("$.data[0].lastPrice").value(4000.00))
                .andExpect(jsonPath("$.data[0].triggered").value(true));

        Integer sameUserAlertCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
        );
        assertThat(sameUserAlertCount).isEqualTo(1);

        Long alertId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Long.class,
                userId,
                productId
        );

        mockMvc.perform(delete("/api/price-alerts/{id}", alertId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
        );
        assertThat(remaining).isZero();
    }

    @Test
    void priceAlertDeleteOnlyRemovesCurrentUserData() throws Exception {
        Long userId = seedUser("price_alert_test_user", "价格提醒测试用户");
        Long otherUserId = seedUser("price_alert_other_user", "其他价格提醒用户");
        Long productId = seedProduct();
        seedReferencePrice(productId, new BigDecimal("6000.00"));
        jdbcTemplate.update("DELETE FROM dc_price_alert WHERE product_id = ?", productId);

        String userToken = jwtTokenProvider.createToken(new LoginUser(userId, "price_alert_test_user", "价格提醒测试用户", "USER"));
        String otherToken = jwtTokenProvider.createToken(new LoginUser(otherUserId, "price_alert_other_user", "其他价格提醒用户", "USER"));

        mockMvc.perform(post("/api/price-alerts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"targetPrice":5000,"priceType":"official"}
                                """.formatted(productId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/price-alerts")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"targetPrice":5500,"priceType":"official"}
                                """.formatted(productId)))
                .andExpect(status().isOk());

        Long otherAlertId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Long.class,
                otherUserId,
                productId
        );

        mockMvc.perform(delete("/api/price-alerts/{id}", otherAlertId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        Integer otherRemaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_price_alert WHERE user_id = ? AND product_id = ?",
                Integer.class,
                otherUserId,
                productId
        );
        assertThat(otherRemaining).isEqualTo(1);
    }

    private Long seedUser(String username, String nickname) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (username, password_hash, nickname, role, status)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  nickname = VALUES(nickname),
                  role = VALUES(role),
                  status = VALUES(status)
                """,
                username,
                "$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS",
                nickname,
                "USER",
                1
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_user WHERE username = ?", Long.class, username);
    }

    private Long seedProduct() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  name = VALUES(name),
                  description = VALUES(description),
                  enabled = VALUES(enabled)
                """,
                "price-alert-test",
                "价格提醒测试分类",
                "价格提醒接口测试使用",
                997,
                1
        );
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = ?",
                Long.class,
                "price-alert-test"
        );

        jdbcTemplate.update(
                """
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "PriceAlertTestBrand",
                997
        );
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = ?",
                Long.class,
                "PriceAlertTestBrand"
        );

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
                "PriceAlertTestBrand",
                "Price Alert Test Product",
                "price-alert-test-model",
                "价格提醒接口测试产品",
                6999,
                81,
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
                "Price Alert Test Product",
                "price-alert-test-model"
        );
    }

    private void seedReferencePrice(Long productId, BigDecimal avgPrice) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, ?, ?)
                ON DUPLICATE KEY UPDATE
                  min_price = VALUES(min_price),
                  max_price = VALUES(max_price),
                  avg_price = VALUES(avg_price),
                  sample_count = VALUES(sample_count),
                  source_type = VALUES(source_type),
                  remark = VALUES(remark)
                """,
                productId,
                "official",
                "PriceAlertTestMarket",
                avgPrice,
                avgPrice,
                avgPrice,
                1,
                "manual",
                "价格提醒接口测试价格"
        );
    }
}
