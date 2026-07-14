package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.a0000.digicompass.common.security.JwtTokenProvider;
import com.a0000.digicompass.modules.ai.client.AiCallException;
import com.a0000.digicompass.modules.ai.client.OpenAiCompatibleChatClient;
import com.a0000.digicompass.modules.ai.crypto.AiKeyCrypto;
import com.a0000.digicompass.modules.auth.dto.LoginUser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class AssistantChatTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AiKeyCrypto aiKeyCrypto;

    @MockBean
    private OpenAiCompatibleChatClient chatClient;

    private Long productId;
    private Long knowledgeId;
    private List<Long> previousDefaultProviderIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(chatClient.chat(any(), anyString(), anyString()))
                .thenThrow(new AiCallException("测试环境模拟 AI 调用失败"));
        seedUser(3001L, "assistant_user_a");
        seedUser(3002L, "assistant_user_b");
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'AssistantTest'");
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('assistant_test_phone', '助手测试手机', '助手测试分类', 997, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name), enabled = VALUES(enabled)
                """);
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = 'assistant_test_phone'",
                Long.class
        );

        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES ('AssistantTest', 997)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """);
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = 'AssistantTest'",
                Long.class
        );

        jdbcTemplate.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, cover_url, official_price, score, status)
                VALUES (?, ?, 'AssistantTest', '助手测试拍照手机', 'assistant-camera', '适合拍照和日常使用', 'https://example.com/phone.png', 3999, 88, 1)
                """, categoryId, brandId);
        productId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_product WHERE category_id = ? AND brand_id = ? AND model = 'assistant-camera'",
                Long.class,
                categoryId,
                brandId
        );
        jdbcTemplate.update("""
                INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
                VALUES (?, 'scene', '拍照旅行', '适合拍照需求', 10)
                """, productId);
        jdbcTemplate.update("""
                INSERT INTO dc_ai_knowledge (category_id, product_id, title, content, knowledge_type, tags, source, status)
                VALUES (?, ?, '测试知识：助手测试拍照手机', '助手测试拍照手机主打拍照旅行，预算 4000 内适合重视影像的用户。', 'product', '拍照,旅行,预算4000', 'assistant-chat-test', 1)
                ON DUPLICATE KEY UPDATE content = VALUES(content), product_id = VALUES(product_id), category_id = VALUES(category_id), status = VALUES(status)
                """, categoryId, productId);
        knowledgeId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_ai_knowledge WHERE title = '测试知识：助手测试拍照手机'",
                Long.class
        );
        jdbcTemplate.update("DELETE FROM dc_ai_knowledge_chunk WHERE knowledge_id = ?", knowledgeId);
        jdbcTemplate.update("""
                INSERT INTO dc_ai_knowledge_chunk (knowledge_id, product_id, category_id, chunk_index, title, content, content_hash, char_count, status)
                VALUES (?, ?, ?, 0, '测试知识：助手测试拍照手机', '助手测试拍照手机适合拍照旅行，预算 4000 内优先推荐。', 'assistant-chat-test-hash', 33, 1)
                """, knowledgeId, productId, categoryId);
    }

    @AfterEach
    void tearDown() {
        if (knowledgeId != null) {
            jdbcTemplate.update("DELETE FROM dc_ai_knowledge_chunk WHERE knowledge_id = ?", knowledgeId);
            jdbcTemplate.update("DELETE FROM dc_ai_knowledge WHERE id = ?", knowledgeId);
        }
        jdbcTemplate.update("DELETE FROM dc_ai_provider_config WHERE provider_code = 'assistant_ai_test'");
        if (!previousDefaultProviderIds.isEmpty()) {
            for (Long id : previousDefaultProviderIds) {
                jdbcTemplate.update("UPDATE dc_ai_provider_config SET default_provider = 1 WHERE id = ?", id);
            }
        }
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'AssistantTest'");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username IN ('assistant_user_a', 'assistant_user_b')");
    }

    @Test
    void assistantConversationsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/assistant/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanCreateConversationAndReceiveProductCards() throws Exception {
        Long conversationId = createConversation(userToken(3001L, "assistant_user_a"));

        mockMvc.perform(post("/api/assistant/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + userToken(3001L, "assistant_user_a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"预算 4000，想要拍照好的手机"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(conversationId))
                .andExpect(jsonPath("$.data.userMessage.role").value("USER"))
                .andExpect(jsonPath("$.data.assistantMessage.role").value("ASSISTANT"))
                .andExpect(jsonPath("$.data.recommendations[0].productId").value(productId))
                .andExpect(jsonPath("$.data.recommendations[0].productName").value("助手测试拍照手机"))
                .andExpect(jsonPath("$.data.recommendations[0].reason").value(org.hamcrest.Matchers.containsString("知识库匹配")))
                .andExpect(jsonPath("$.data.recommendations[0].coverUrl").value("https://example.com/phone.png"))
                .andExpect(jsonPath("$.data.recommendations[0].detailPath").value("/products/" + productId))
                .andExpect(jsonPath("$.data.recommendations[0].matchTags[0]").isString())
                .andExpect(jsonPath("$.data.recommendations[0].cautionTags[0]").isString())
                .andExpect(jsonPath("$.data.recommendations[0].nextActions[0]").value("查看详情"))
                .andExpect(jsonPath("$.data.recommendations[0].explainSummary").value(org.hamcrest.Matchers.containsString("推荐依据")))
                .andExpect(jsonPath("$.data.recommendations[0].matchedRequirements[0]").value(org.hamcrest.Matchers.containsString("预算")))
                .andExpect(jsonPath("$.data.recommendations[0].knowledgeEvidence[0]").value(org.hamcrest.Matchers.containsString("测试知识")))
                .andExpect(jsonPath("$.data.recommendations[0].tradeoffNotes[0]").isString());

        mockMvc.perform(get("/api/assistant/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + userToken(3001L, "assistant_user_a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.messages.length()").value(2))
                .andExpect(jsonPath("$.data.recommendations.length()").value(4))
                .andExpect(jsonPath("$.data.recommendations[0].productId").value(productId))
                .andExpect(jsonPath("$.data.recommendations[0].knowledgeEvidence[0]").value(org.hamcrest.Matchers.containsString("测试知识")));

        Integer logCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_ai_workflow_log WHERE conversation_id = ?",
                Integer.class,
                conversationId
        );
        assertThat(logCount).isEqualTo(1);

        String retrievedContextSummary = jdbcTemplate.queryForObject(
                "SELECT retrieved_context_summary FROM dc_ai_workflow_log WHERE conversation_id = ?",
                String.class,
                conversationId
        );
        assertThat(retrievedContextSummary).contains("测试知识：助手测试拍照手机");
    }

    @Test
    void assistantUsesAiToEnhanceRecommendationText() throws Exception {
        seedDefaultAiProvider();
        doReturn("""
                        {
                          "categoryKeyword": "手机",
                          "minBudget": null,
                          "maxBudget": 4000,
                          "acceptUsed": false,
                          "usageScenes": ["摄影"],
                          "preferredBrands": [],
                          "avoidedBrands": [],
                          "priorityTags": ["影像"],
                          "avoidTags": [],
                          "riskPreference": "medium",
                          "rawKeywords": "预算 4000 拍照手机"
                        }
                        """)
                .doReturn("""
                        {
                          "items": [
                            {
                              "productId": %d,
                              "reason": "AI推荐理由：这台机器更适合拍照和日常记录。",
                              "riskTip": "AI避坑提示：重点确认电池健康和售后。"
                            }
                          ]
                        }
                        """.formatted(productId))
                .when(chatClient).chat(any(), anyString(), anyString());
        Long conversationId = createConversation(userToken(3001L, "assistant_user_a"));

        mockMvc.perform(post("/api/assistant/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + userToken(3001L, "assistant_user_a"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"预算 4000，想要拍照好的手机"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendations[0].productId").value(productId))
                .andExpect(jsonPath("$.data.recommendations[0].reason").value("AI推荐理由：这台机器更适合拍照和日常记录。"))
                .andExpect(jsonPath("$.data.recommendations[0].riskTip").value("AI避坑提示：重点确认电池健康和售后。"));
    }

    @Test
    void userCannotReadOtherUsersConversation() throws Exception {
        Long conversationId = createConversation(userToken(3001L, "assistant_user_a"));

        mockMvc.perform(get("/api/assistant/conversations/{id}/messages", conversationId)
                        .header("Authorization", "Bearer " + userToken(3002L, "assistant_user_b")))
                .andExpect(status().isForbidden());
    }

    private Long createConversation(String token) throws Exception {
        String body = mockMvc.perform(post("/api/assistant/conversations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"测试选购咨询"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private String userToken(Long userId, String username) {
        return jwtTokenProvider.createToken(new LoginUser(userId, username, username, "USER"));
    }

    private void seedUser(Long id, String username) {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (id, username, password_hash, nickname, role, status)
                VALUES (?, ?, '$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS', ?, 'USER', 1)
                ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status)
                """,
                id,
                username,
                username
        );
    }

    private void seedDefaultAiProvider() {
        previousDefaultProviderIds = jdbcTemplate.queryForList(
                "SELECT id FROM dc_ai_provider_config WHERE default_provider = 1",
                Long.class
        );
        jdbcTemplate.update("UPDATE dc_ai_provider_config SET default_provider = 0");
        jdbcTemplate.update("""
                INSERT INTO dc_ai_provider_config
                (provider_code, provider_name, protocol_type, base_url, api_key_cipher, chat_model, embedding_model,
                 temperature, max_tokens, million_context, timeout_seconds, enabled, default_provider, remark)
                VALUES ('assistant_ai_test', '助手测试 AI', 'openai-compatible', 'https://api.example.com/v1', ?,
                        'assistant-test-model', null, 0.2, 512, 0, 30, 1, 1, 'assistant chat test')
                ON DUPLICATE KEY UPDATE api_key_cipher = VALUES(api_key_cipher), enabled = 1, default_provider = 1
                """, aiKeyCrypto.encrypt("sk-test"));
    }
}
