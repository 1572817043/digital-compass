package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminFoundationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        seedUser(9301L, "foundation_admin", "ADMIN");
        seedUser(9302L, "foundation_user", "USER");
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('foundation_category', '基础模块测试分类', '测试分类', 998, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), enabled = VALUES(enabled)
                """);
        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES ('FoundationBrand', 998)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM dc_category WHERE code LIKE 'foundation_%'");
        jdbcTemplate.update("DELETE FROM dc_brand WHERE name IN ('FoundationBrand', 'FoundationBrandNew')");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username IN ('foundation_admin', 'foundation_user')");
    }

    @Test
    void dashboardRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/overview"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + token(9302L, "foundation_user", "USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/dashboard/overview")
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productCount").isNumber())
                .andExpect(jsonPath("$.data.userCount").isNumber())
                .andExpect(jsonPath("$.data.aiWorkflowLogCount").isNumber());
    }

    @Test
    void adminCanResetUserPasswordWithBcryptHash() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/password/reset", 9302L)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk());

        String hash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM dc_user WHERE id = 9302",
                String.class
        );
        assertThat(hash).isNotEqualTo("123456");
        assertThat(passwordEncoder.matches("123456", hash)).isTrue();
    }

    @Test
    void adminCannotDisableOrDemoteSelf() throws Exception {
        mockMvc.perform(put("/api/admin/users/{id}/status", 9301L)
                        .header("Authorization", "Bearer " + adminToken())
                        .param("status", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/admin/users/{id}/role", 9301L)
                        .header("Authorization", "Bearer " + adminToken())
                        .param("role", "USER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void taxonomyRejectsBlankUpdatePayload() throws Exception {
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = 'foundation_category'",
                Long.class
        );
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = 'FoundationBrand'",
                Long.class
        );

        mockMvc.perform(put("/api/admin/categories/{id}", categoryId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"","name":""}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/admin/brands/{id}", brandId)
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    private void seedUser(Long id, String username, String role) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (id, username, password_hash, nickname, role, status)
                VALUES (?, ?, '$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS', ?, ?, 1)
                ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status)
                """,
                id,
                username,
                username,
                role
        );
    }

    private String adminToken() {
        return token(9301L, "foundation_admin", "ADMIN");
    }

    private String token(Long userId, String username, String role) {
        return jwtTokenProvider.createToken(new LoginUser(userId, username, username, role));
    }
}
