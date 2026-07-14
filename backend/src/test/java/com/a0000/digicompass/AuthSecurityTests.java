package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
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
class AuthSecurityTests {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void bcryptUsesRandomSaltAndCanVerifyPassword() {
        String rawPassword = "123456";

        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        assertThat(firstHash).isNotEqualTo(secondHash);
        assertThat(passwordEncoder.matches(rawPassword, firstHash)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, secondHash)).isTrue();
    }

    @Test
    void jwtCanCreateAndParseLoginUser() {
        LoginUser loginUser = new LoginUser(1L, "admin", "管理员", "ADMIN");

        String token = jwtTokenProvider.createToken(loginUser);
        LoginUser parsedUser = jwtTokenProvider.parseToken(token);

        assertThat(parsedUser.username()).isEqualTo("admin");
        assertThat(parsedUser.role()).isEqualTo("ADMIN");
    }

    @Test
    void currentUserEndpointRequiresJwtToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsJwtTokenForValidPassword() throws Exception {
        seedTestAdmin();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"auth_test_admin","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("auth_test_admin"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    void loginRejectsInvalidPassword() throws Exception {
        seedTestAdmin();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"auth_test_admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void seedAdminPasswordMatchesDefaultPassword() throws Exception {
        String seed = Files.readString(Path.of("src/main/resources/db/seed.sql"));
        String adminHash = Pattern.compile("VALUES \\('admin', '([^']+)'")
                .matcher(seed)
                .results()
                .findFirst()
                .orElseThrow()
                .group(1);

        assertThat(passwordEncoder.matches("123456", adminHash)).isTrue();
    }

    private void seedTestAdmin() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (username, password_hash, nickname, role, status)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  password_hash = VALUES(password_hash),
                  nickname = VALUES(nickname),
                  role = VALUES(role),
                  status = VALUES(status)
                """,
                "auth_test_admin",
                "$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS",
                "认证测试管理员",
                "ADMIN",
                1
        );
    }
}
