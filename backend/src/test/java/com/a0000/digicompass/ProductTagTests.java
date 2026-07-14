package com.a0000.digicompass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductTagTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void productDetailReturnsDecisionTags() throws Exception {
        Long productId = seedProduct();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags").isArray());
    }

    @Test
    void adminCanListProductTags() throws Exception {
        Long productId = seedProduct();

        mockMvc.perform(get("/api/admin/products/{id}/tags", productId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void adminCanCreateUpdateAndDeleteProductTag() throws Exception {
        Long productId = seedProduct();

        String body = mockMvc.perform(post("/api/admin/products/{id}/tags", productId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tagType":"selling_point","tagName":"影像强","tagValue":"适合拍照用户","sortOrder":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long tagId = Long.valueOf(body.replaceAll(".*\"data\":(\\d+).*", "$1"));

        mockMvc.perform(put("/api/admin/products/{id}/tags/{tagId}", productId, tagId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tagType":"weakness","tagName":"价格高","tagValue":"预算敏感用户需谨慎","sortOrder":20}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags[0].tagType").value("weakness"))
                .andExpect(jsonPath("$.data.tags[0].tagName").value("价格高"));

        mockMvc.perform(delete("/api/admin/products/{id}/tags/{tagId}", productId, tagId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tags").isEmpty());
    }

    @Test
    void productTagsRequireAdminRole() throws Exception {
        Long productId = seedProduct();

        mockMvc.perform(get("/api/admin/products/{id}/tags", productId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/products/{id}/tags", productId)
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isForbidden());
    }

    private Long seedProduct() {
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('tag_test_phone', '标签测试手机', '测试分类', 999, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name)
                """);
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = 'tag_test_phone'",
                Long.class
        );

        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES ('TagTest', 999)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """);
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = 'TagTest'",
                Long.class
        );

        jdbcTemplate.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, 'TagTest', '标签测试产品', 'tag-model', '测试标签返回', 3999, 80, 1)
                ON DUPLICATE KEY UPDATE score = VALUES(score), status = 1
                """, categoryId, brandId);

        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_product WHERE category_id = ? AND brand_id = ? AND name = '标签测试产品' AND model = 'tag-model'",
                Long.class,
                categoryId,
                brandId
        );
    }

    private String adminToken() {
        return jwtTokenProvider.createToken(new LoginUser(1001L, "tag_admin", "标签管理员", "ADMIN"));
    }

    private String userToken() {
        return jwtTokenProvider.createToken(new LoginUser(1002L, "tag_user", "标签用户", "USER"));
    }
}
