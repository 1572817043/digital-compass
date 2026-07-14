package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProductImageControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void cleanTestData() {
        jdbcTemplate.update("""
                DELETE FROM dc_product_image
                WHERE product_id IN (
                    SELECT id FROM dc_product
                    WHERE name = ? AND model = ?
                )
                """, "Admin Image Test Product", "admin-image-test-model");
        jdbcTemplate.update("DELETE FROM dc_product WHERE name = ? AND model = ?",
                "Admin Image Test Product", "admin-image-test-model");
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name = ?", "AdminImageTestBrand");
        jdbcTemplate.update("DELETE FROM dc_category WHERE code = ?", "admin-image-test");
    }

    @Test
    void bindingNewMainImageKeepsOnlyOneMainImage() throws Exception {
        Long productId = seedProduct();
        String token = adminToken();

        mockMvc.perform(post("/api/admin/products/{productId}/images", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imageUrl":"https://example.com/old-main.png","imageType":"MAIN","sortOrder":0}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/products/{productId}/images", productId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imageUrl":"https://example.com/new-main.png","imageType":"MAIN","sortOrder":0}
                                """))
                .andExpect(status().isOk());

        Integer mainCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_product_image WHERE product_id = ? AND image_type = 'MAIN'",
                Integer.class,
                productId
        );
        assertThat(mainCount).isEqualTo(1);

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.images[0].imageUrl").value("https://example.com/new-main.png"));
    }

    private Long seedProduct() {
        Long categoryId = seedCategory();
        Long brandId = seedBrand();
        jdbcTemplate.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                categoryId,
                brandId,
                "AdminImageTestBrand",
                "Admin Image Test Product",
                "admin-image-test-model",
                "管理端图片测试产品",
                3999,
                80,
                1
        );
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_product WHERE name = ? AND model = ?",
                Long.class,
                "Admin Image Test Product",
                "admin-image-test-model"
        );
    }

    private Long seedCategory() {
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE name = VALUES(name), enabled = VALUES(enabled)
                """,
                "admin-image-test",
                "管理端图片测试分类",
                "管理端图片接口测试使用",
                994,
                1
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_category WHERE code = ?", Long.class, "admin-image-test");
    }

    private Long seedBrand() {
        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """,
                "AdminImageTestBrand",
                994
        );
        return jdbcTemplate.queryForObject("SELECT id FROM dc_brand WHERE name = ?", Long.class, "AdminImageTestBrand");
    }

    private String adminToken() {
        return jwtTokenProvider.createToken(new LoginUser(9101L, "admin_image_admin", "管理端图片测试管理员", "ADMIN"));
    }
}
