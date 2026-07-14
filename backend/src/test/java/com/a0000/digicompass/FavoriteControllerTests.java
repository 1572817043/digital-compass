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
class FavoriteControllerTests {

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
                DELETE FROM dc_user_favorite
                WHERE user_id IN (SELECT id FROM dc_user WHERE username = ?)
                   OR product_id IN (SELECT id FROM dc_product WHERE name = ? AND model = ?)
                """,
                "favorite_test_user",
                "Favorite Test Product",
                "favorite-test-model"
        );
        jdbcTemplate.update(
                "DELETE FROM dc_product WHERE name = ? AND model = ?",
                "Favorite Test Product",
                "favorite-test-model"
        );
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "FavoriteTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "favorite-test");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username = ?", "favorite_test_user");
    }

    @Test
    void favoriteEndpointsRequireJwt() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/favorites/ids"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/favorites/{productId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void favoriteFlowCanAddListAndRemoveProduct() throws Exception {
        Long userId = seedUser();
        Long productId = seedProduct();
        jdbcTemplate.update("DELETE FROM dc_user_favorite WHERE user_id = ? AND product_id = ?", userId, productId);

        String token = jwtTokenProvider.createToken(new LoginUser(userId, "favorite_test_user", "收藏测试用户", "USER"));

        mockMvc.perform(post("/api/favorites/{productId}", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/favorites/{productId}", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer favoriteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_user_favorite WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
        );
        assertThat(favoriteCount).isEqualTo(1);

        mockMvc.perform(get("/api/favorites/ids")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(productId.intValue()));

        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(productId.intValue()));

        mockMvc.perform(delete("/api/favorites/{productId}", productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_user_favorite WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
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
                "favorite_test_user",
                "$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS",
                "收藏测试用户",
                "USER",
                1
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_user WHERE username = ?",
                Long.class,
                "favorite_test_user"
        );
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
                "favorite-test",
                "收藏测试分类",
                "收藏接口测试使用",
                999,
                1
        );
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = ?",
                Long.class,
                "favorite-test"
        );

        jdbcTemplate.update(
                """
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "FavoriteTestBrand",
                999
        );
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = ?",
                Long.class,
                "FavoriteTestBrand"
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
                "FavoriteTestBrand",
                "Favorite Test Product",
                "favorite-test-model",
                "收藏接口测试产品",
                1999,
                80,
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
                "Favorite Test Product",
                "favorite-test-model"
        );
    }
}
