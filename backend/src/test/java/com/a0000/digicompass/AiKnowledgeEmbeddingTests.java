package com.a0000.digicompass;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiKnowledgeEmbeddingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Long phoneProductId;
    private Long gameProductId;
    private Long phoneKnowledgeId;
    private Long gameKnowledgeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM dc_ai_knowledge_chunk WHERE title LIKE '向量知识：%'");
        jdbcTemplate.update("DELETE FROM dc_ai_knowledge WHERE source = 'embedding-test'");
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'EmbeddingTest'");
        jdbcTemplate.update("""
                INSERT INTO dc_category (code, name, description, sort_order, enabled)
                VALUES ('embedding_test_phone', '向量测试手机', '向量测试分类', 996, 1)
                ON DUPLICATE KEY UPDATE name = VALUES(name), enabled = VALUES(enabled)
                """);
        Long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_category WHERE code = 'embedding_test_phone'",
                Long.class
        );
        jdbcTemplate.update("""
                INSERT INTO dc_brand (name, sort_order)
                VALUES ('EmbeddingTest', 996)
                ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order)
                """);
        Long brandId = jdbcTemplate.queryForObject(
                "SELECT id FROM dc_brand WHERE name = 'EmbeddingTest'",
                Long.class
        );
        phoneProductId = seedProduct(categoryId, brandId, "向量测试拍照手机", "embedding-camera", "拍照旅行预算友好", 88);
        gameProductId = seedProduct(categoryId, brandId, "向量测试游戏手机", "embedding-game", "游戏性能散热优先", 80);
        phoneKnowledgeId = seedKnowledge(categoryId, phoneProductId, "向量知识：拍照手机", "拍照 旅行 预算 4000 手机 影像 长焦 人像", "camera-hash");
        gameKnowledgeId = seedKnowledge(categoryId, gameProductId, "向量知识：游戏手机", "游戏 高帧率 散热 性能 电竞 手机", "game-hash");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM dc_ai_knowledge_chunk WHERE title LIKE '向量知识：%'");
        jdbcTemplate.update("DELETE FROM dc_ai_knowledge WHERE source = 'embedding-test'");
        jdbcTemplate.update("DELETE FROM dc_product WHERE brand = 'EmbeddingTest'");
    }

    @Test
    void adminCanRebuildAndSearchKnowledgeEmbeddings() throws Exception {
        String token = jwtTokenProvider.createToken(new LoginUser(9001L, "embedding_admin", "向量管理员", "ADMIN"));

        mockMvc.perform(post("/api/admin/ai/knowledge/embeddings/rebuild")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());

        Integer embeddingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dc_ai_knowledge_embedding WHERE chunk_id IN (SELECT id FROM dc_ai_knowledge_chunk WHERE knowledge_id IN (?, ?))",
                Integer.class,
                phoneKnowledgeId,
                gameKnowledgeId
        );
        assertThat(embeddingCount).isEqualTo(2);

        mockMvc.perform(get("/api/admin/ai/knowledge/embeddings/search")
                        .header("Authorization", "Bearer " + token)
                        .param("query", "预算 4000 拍照 旅行 手机")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(phoneProductId))
                .andExpect(jsonPath("$.data[0].title").value("向量知识：拍照手机"))
                .andExpect(jsonPath("$.data[0].retrievalSource").value("VECTOR"))
                .andExpect(jsonPath("$.data[0].embeddingModel").isString());
    }

    private Long seedProduct(Long categoryId, Long brandId, String name, String model, String summary, int score) {
        jdbcTemplate.update("""
                INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, score, status)
                VALUES (?, ?, 'EmbeddingTest', ?, ?, ?, 3999, ?, 1)
                """, categoryId, brandId, name, model, summary, score);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM dc_product WHERE category_id = ? AND brand_id = ? AND model = ?",
                Long.class,
                categoryId,
                brandId,
                model
        );
    }

    private Long seedKnowledge(Long categoryId, Long productId, String title, String content, String hash) {
        jdbcTemplate.update("""
                INSERT INTO dc_ai_knowledge (category_id, product_id, title, content, knowledge_type, tags, source, status)
                VALUES (?, ?, ?, ?, 'product', null, 'embedding-test', 1)
                """, categoryId, productId, title, content);
        Long knowledgeId = jdbcTemplate.queryForObject("SELECT id FROM dc_ai_knowledge WHERE title = ?", Long.class, title);
        jdbcTemplate.update("""
                INSERT INTO dc_ai_knowledge_chunk (knowledge_id, product_id, category_id, chunk_index, title, content, content_hash, char_count, status)
                VALUES (?, ?, ?, 1, ?, ?, ?, ?, 1)
                """, knowledgeId, productId, categoryId, title, content, hash, content.length());
        return knowledgeId;
    }
}
