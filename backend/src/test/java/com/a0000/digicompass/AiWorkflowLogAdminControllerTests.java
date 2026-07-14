package com.a0000.digicompass;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiWorkflowLogAdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long logId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (id, username, password_hash, nickname, role, status)
                VALUES (9101, 'workflow_admin', '$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS', '工作流管理员', 'ADMIN', 1)
                ON DUPLICATE KEY UPDATE role = VALUES(role), status = VALUES(status)
                """
        );
        jdbcTemplate.update(
                """
                INSERT INTO dc_user (id, username, password_hash, nickname, role, status)
                VALUES (9102, 'workflow_user', '$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS', '普通用户', 'USER', 1)
                ON DUPLICATE KEY UPDATE role = VALUES(role), status = VALUES(status)
                """
        );
        jdbcTemplate.update(
                """
                INSERT INTO dc_ai_workflow_log
                  (user_id, conversation_id, provider_id, model_name, user_requirement,
                   parsed_requirement_json, retrieved_context_summary, candidate_product_ids,
                   fallback_used, error_message)
                VALUES (9102, 12345, NULL, 'mimo-v2.5-pro', '预算 4000，拍照手机',
                        '{"categoryKeyword":"手机","maxBudget":4000}',
                        '测试知识：拍照手机适合旅行用户',
                        '1,2,3', 0, NULL)
                """
        );
        logId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_ai_workflow_log WHERE conversation_id = 12345 ORDER BY id DESC LIMIT 1",
                Long.class
        );
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM dc_ai_workflow_log WHERE conversation_id = 12345");
        jdbcTemplate.update("DELETE FROM dc_user WHERE username IN ('workflow_admin', 'workflow_user')");
    }

    @Test
    void adminCanListAiWorkflowLogs() throws Exception {
        mockMvc.perform(get("/api/admin/ai/workflow-logs")
                        .header("Authorization", "Bearer " + token(9101L, "workflow_admin", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(logId))
                .andExpect(jsonPath("$.data[0].username").value("workflow_user"))
                .andExpect(jsonPath("$.data[0].modelName").value("mimo-v2.5-pro"))
                .andExpect(jsonPath("$.data[0].userRequirement").value("预算 4000，拍照手机"))
                .andExpect(jsonPath("$.data[0].parsedRequirementJson").value("{\"categoryKeyword\":\"手机\",\"maxBudget\":4000}"))
                .andExpect(jsonPath("$.data[0].retrievedContextSummary").value("测试知识：拍照手机适合旅行用户"))
                .andExpect(jsonPath("$.data[0].candidateProductIds").value("1,2,3"))
                .andExpect(jsonPath("$.data[0].fallbackUsed").value(false));
    }

    @Test
    void adminCanReadAiWorkflowLogDetail() throws Exception {
        mockMvc.perform(get("/api/admin/ai/workflow-logs/{id}", logId)
                        .header("Authorization", "Bearer " + token(9101L, "workflow_admin", "ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(logId))
                .andExpect(jsonPath("$.data.parsedRequirement.categoryKeyword").value("手机"))
                .andExpect(jsonPath("$.data.parsedRequirement.maxBudget").value(4000))
                .andExpect(jsonPath("$.data.retrievedContexts[0]").value("测试知识：拍照手机适合旅行用户"))
                .andExpect(jsonPath("$.data.candidateProductIds[0]").value(1))
                .andExpect(jsonPath("$.data.workflowSteps[0].name").value("用户输入"))
                .andExpect(jsonPath("$.data.workflowSteps[1].name").value("需求解析"))
                .andExpect(jsonPath("$.data.workflowSteps[2].name").value("RAG 检索"))
                .andExpect(jsonPath("$.data.workflowSteps[3].name").value("候选产品"))
                .andExpect(jsonPath("$.data.workflowSteps[4].name").value("推荐输出"));
    }

    @Test
    void normalUserCannotListAiWorkflowLogs() throws Exception {
        mockMvc.perform(get("/api/admin/ai/workflow-logs")
                        .header("Authorization", "Bearer " + token(9102L, "workflow_user", "USER")))
                .andExpect(status().isForbidden());
    }

    private String token(Long userId, String username, String role) {
        return jwtTokenProvider.createToken(new LoginUser(userId, username, username, role));
    }
}
