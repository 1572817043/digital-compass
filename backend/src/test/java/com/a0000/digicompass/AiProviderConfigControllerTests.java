package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleModelClient;
import com.a0000.digicompass.modules.ai.dto.AiModelItem;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiProviderConfigControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private OpenAiCompatibleModelClient modelClient;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM dc_ai_provider_config WHERE provider_code = 'mimo-test-openai-compatible'");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username = 'ai_provider_admin'");
    }

    @Test
    void schemaContainsProviderProtocolType() throws Exception {
        String schema = Files.readString(Path.of("src/main/resources/db/schema.sql"));

        assertThat(schema).contains("protocol_type");
        assertThat(schema).contains("million_context");
    }

    @Test
    void adminCanSaveProviderProtocolAndReceiveMaskedKey() throws Exception {
        seedAdmin();

        String response = mockMvc.perform(post("/api/admin/ai/providers")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "providerCode": "mimo-test-openai-compatible",
                                  "providerName": "小米 Mimo 测试",
                                  "protocolType": "openai-compatible",
                                  "baseUrl": "https://api.xiaomimimo.com",
                                  "apiKey": "sk-test-ai-provider",
                                  "chatModel": "mimo-chat",
                                  "embeddingModel": "mimo-embedding",
                                  "temperature": 0.3,
                                  "maxTokens": 512,
                                  "timeoutSeconds": 20,
                                  "millionContext": true,
                                  "enabled": true,
                                  "defaultProvider": false,
                                  "remark": "测试配置"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long providerId = Long.valueOf(response.replaceAll(".*\"data\":(\\d+).*", "$1"));

        mockMvc.perform(get("/api/admin/ai/providers/{id}", providerId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.providerCode").value("mimo-test-openai-compatible"))
                .andExpect(jsonPath("$.data.protocolType").value("openai-compatible"))
                .andExpect(jsonPath("$.data.baseUrl").value("https://api.xiaomimimo.com"))
                .andExpect(jsonPath("$.data.maskedApiKey").value("sk-t****ider"))
                .andExpect(jsonPath("$.data.embeddingModel").value("mimo-embedding"))
                .andExpect(jsonPath("$.data.millionContext").value(true));
    }

    @Test
    void adminCanFetchModelsFromTemporaryConfig() throws Exception {
        seedAdmin();
        when(modelClient.listModels(any())).thenReturn(List.of(
                new AiModelItem("mimo-v2.5-pro", "xiaomi"),
                new AiModelItem("mimo-v2.5", "xiaomi")
        ));

        mockMvc.perform(post("/api/admin/ai/providers/models")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "providerCode": "mimo-temp",
                                  "providerName": "小米 Mimo 临时测试",
                                  "protocolType": "openai-compatible",
                                  "baseUrl": "https://api.xiaomimimo.com/v1",
                                  "apiKey": "sk-temp",
                                  "chatModel": "",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("mimo-v2.5-pro"))
                .andExpect(jsonPath("$.data[0].ownedBy").value("xiaomi"));
    }

    @Test
    void adminCanFetchModelsFromSavedProvider() throws Exception {
        seedAdmin();
        when(modelClient.listModels(any())).thenReturn(List.of(
                new AiModelItem("mimo-v2.5-pro", "xiaomi")
        ));

        String response = mockMvc.perform(post("/api/admin/ai/providers")
                        .header("Authorization", "Bearer " + adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "providerCode": "mimo-test-openai-compatible",
                                  "providerName": "小米 Mimo 测试",
                                  "protocolType": "openai-compatible",
                                  "baseUrl": "https://api.xiaomimimo.com/v1",
                                  "apiKey": "sk-test-ai-provider",
                                  "chatModel": "mimo-v2.5-pro",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long providerId = Long.valueOf(response.replaceAll(".*\"data\":(\\d+).*", "$1"));

        mockMvc.perform(get("/api/admin/ai/providers/{id}/models", providerId)
                        .header("Authorization", "Bearer " + adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value("mimo-v2.5-pro"));
    }

    private void seedAdmin() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (username, password_hash, nickname, role, status)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE role = VALUES(role), status = VALUES(status)
                """,
                "ai_provider_admin",
                "$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS",
                "AI配置测试管理员",
                "ADMIN",
                1
        );
    }

    private String adminToken() {
        return jwtTokenProvider.createToken(new LoginUser(9901L, "ai_provider_admin", "AI配置测试管理员", "ADMIN"));
    }
}
