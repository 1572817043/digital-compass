package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HistoryControllerTests {

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
                DELETE FROM dc_user_view_history
                WHERE user_id IN (SELECT id FROM dc_user WHERE username = ?)
                   OR product_id IN (
                       SELECT id FROM dc_product
                       WHERE name IN (?, ?) AND model IN (?, ?)
                   )
                """,
                "history_test_user",
                "History Test Product A",
                "History Test Product B",
                "history-test-model-a",
                "history-test-model-b"
        );
        jdbcTemplate.update(
                "DELETE FROM dc_product WHERE name IN (?, ?) AND model IN (?, ?)",
                "History Test Product A",
                "History Test Product B",
                "history-test-model-a",
                "history-test-model-b"
        );
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "HistoryTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "history-test");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username = ?", "history_test_user");
    }

    @Test
    void historyEndpointsRequireJwt() throws Exception {
        mockMvc.perform(get("/api/history/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/history/products/{productId}", 1L))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/history/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void historyFlowCanRecordListUpdateOrderAndClear() throws Exception {
        Long userId = seedUser();
        Long firstProductId = seedProduct("History Test Product A", "history-test-model-a", 78);
        Long secondProductId = seedProduct("History Test Product B", "history-test-model-b", 88);
        jdbcTemplate.update("DELETE FROM dc_user_view_history WHERE user_id = ?", userId);

        String token = jwtTokenProvider.createToken(new LoginUser(userId, "history_test_user", "历史测试用户", "USER"));

        mockMvc.perform(post("/api/history/products/{productId}", firstProductId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/history/products/{productId}", firstProductId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer duplicatedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_user_view_history WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                firstProductId
        );
        assertThat(duplicatedCount).isEqualTo(1);

        Thread.sleep(20);

        mockMvc.perform(post("/api/history/products/{productId}", secondProductId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/history/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(secondProductId.intValue()))
                .andExpect(jsonPath("$.data[1].id").value(firstProductId.intValue()));

        mockMvc.perform(delete("/api/history/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_user_view_history WHERE user_id = ?",
                Integer.class,
                userId
        );
        assertThat(remaining).isZero();
    }

    private Long seedUser() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (username, password_hash, nickname, role, status)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  nickname = VALUES(nickname),
                  role = VALUES(role),
                  status = VALUES(status)
                """,
                "history_test_user",
                "$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS",
                "历史测试用户",
                "USER",
                1
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_user WHERE username = ?",
                Long.class,
                "history_test_user"
        );
    }

    private Long seedProduct(String name, String model, int score) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  name = VALUES(name),
                  description = VALUES(description),
                  enabled = VALUES(enabled)
                """,
                "history-test",
                "历史测试分类",
                "浏览历史接口测试使用",
                998,
                1
        );
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = ?",
                Long.class,
                "history-test"
        );

        jdbcTemplate.update(
                """
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "HistoryTestBrand",
                998
        );
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = ?",
                Long.class,
                "HistoryTestBrand"
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
                "HistoryTestBrand",
                name,
                model,
                "浏览历史接口测试产品",
                1999,
                score,
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
                name,
                model
        );
    }
}
