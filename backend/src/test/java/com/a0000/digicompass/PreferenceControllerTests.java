package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class PreferenceControllerTests {

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
                DELETE FROM dc_user_preference
                WHERE user_id IN (SELECT id FROM dc_user WHERE username IN (?, ?))
                """,
                "preference_test_user",
                "preference_other_user"
        );
        jdbcTemplate.update("DELETE FROM dc_user WHERE username IN (?, ?)", "preference_test_user", "preference_other_user");
    }

    @Test
    void preferenceEndpointsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/preferences/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/preferences/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"minBudget":3000,"maxBudget":6000}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void firstGetReturnsDefaultPreferenceShape() throws Exception {
        Long userId = seedUser("preference_test_user", "偏好测试用户");

        mockMvc.perform(get("/api/preferences/me")
                        .header("Authorization", "Bearer " + userToken(userId, "preference_test_user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isEmpty())
                .andExpect(jsonPath("$.data.minBudget").isEmpty())
                .andExpect(jsonPath("$.data.maxBudget").isEmpty())
                .andExpect(jsonPath("$.data.brandIds").isEmpty());
    }

    @Test
    void saveThenGetPreferenceAndKeepSingleRecordPerUser() throws Exception {
        Long userId = seedUser("preference_test_user", "偏好测试用户");
        String token = userToken(userId, "preference_test_user");

        mockMvc.perform(put("/api/preferences/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "minBudget":3000,
                                  "maxBudget":6000,
                                  "categoryId":1,
                                  "brandIds":"1,2",
                                  "usageScenes":"学习,办公",
                                  "priorityTags":"性能,续航",
                                  "avoidTags":"发热,机身重",
                                  "remark":"主要写代码"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/preferences/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.minBudget").value(3000))
                .andExpect(jsonPath("$.data.maxBudget").value(6000))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.brandIds").value("1,2"))
                .andExpect(jsonPath("$.data.usageScenes").value("学习,办公"))
                .andExpect(jsonPath("$.data.priorityTags").value("性能,续航"))
                .andExpect(jsonPath("$.data.avoidTags").value("发热,机身重"))
                .andExpect(jsonPath("$.data.remark").value("主要写代码"));

        mockMvc.perform(put("/api/preferences/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "minBudget":4000,
                                  "maxBudget":8000,
                                  "brandIds":"2",
                                  "usageScenes":"游戏",
                                  "priorityTags":"屏幕",
                                  "avoidTags":"维修贵",
                                  "remark":"更新偏好"
                                }
                                """))
                .andExpect(status().isOk());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_user_preference WHERE user_id = ?",
                Integer.class,
                userId
        );
        assertThat(count).isEqualTo(1);

        mockMvc.perform(get("/api/preferences/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.minBudget").value(4000))
                .andExpect(jsonPath("$.data.maxBudget").value(8000))
                .andExpect(jsonPath("$.data.brandIds").value("2"))
                .andExpect(jsonPath("$.data.usageScenes").value("游戏"))
                .andExpect(jsonPath("$.data.remark").value("更新偏好"));
    }

    @Test
    void saveRejectsInvalidBudgetRange() throws Exception {
        Long userId = seedUser("preference_test_user", "偏好测试用户");

        mockMvc.perform(put("/api/preferences/me")
                        .header("Authorization", "Bearer " + userToken(userId, "preference_test_user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"minBudget":8000,"maxBudget":3000}
                                """))
                .andExpect(status().isBadRequest());
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

    private String userToken(Long userId, String username) {
        return jwtTokenProvider.createToken(new LoginUser(userId, username, "偏好测试用户", "USER"));
    }
}
