package com.a0000.digicompass.modules.assistant.mapper;

import com.a0000.digicompass.modules.assistant.dto.AssistantConversationItem;
import com.a0000.digicompass.modules.assistant.dto.AssistantRecommendationItem;
import com.a0000.digicompass.modules.assistant.dto.ChatMessageItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AssistantMapper {

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public AssistantMapper(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<AssistantConversationItem> findConversations(Long userId) {
        return jdbc.query(
                """
                SELECT c.id, c.title,
                       (
                         SELECT m.content
                         FROM dc_assistant_message m
                         WHERE m.conversation_id = c.id
                         ORDER BY m.created_at DESC, m.id DESC
                         LIMIT 1
                       ) AS last_message,
                       c.created_at, c.updated_at
                FROM dc_assistant_conversation c
                WHERE c.user_id = ?
                ORDER BY c.updated_at DESC, c.id DESC
                """,
                (rs, rowNum) -> new AssistantConversationItem(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("last_message"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                userId
        );
    }

    public Long insertConversation(Long userId, String title) {
        jdbc.update(
                "INSERT INTO dc_assistant_conversation (user_id, title) VALUES (?, ?)",
                userId,
                title
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public boolean conversationBelongsToUser(Long conversationId, Long userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dc_assistant_conversation WHERE id = ? AND user_id = ?",
                Integer.class,
                conversationId,
                userId
        );
        return count != null && count > 0;
    }

    public void touchConversation(Long conversationId) {
        jdbc.update("UPDATE dc_assistant_conversation SET updated_at = NOW() WHERE id = ?", conversationId);
    }

    public void updateConversationTitle(Long conversationId, String title) {
        jdbc.update("UPDATE dc_assistant_conversation SET title = ?, updated_at = NOW() WHERE id = ?", title, conversationId);
    }

    public Long insertMessage(Long conversationId, Long userId, String role, String content) {
        jdbc.update(
                """
                INSERT INTO dc_assistant_message (conversation_id, user_id, role, content)
                VALUES (?, ?, ?, ?)
                """,
                conversationId,
                userId,
                role,
                content
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public ChatMessageItem findMessage(Long messageId) {
        return jdbc.queryForObject(
                """
                SELECT id, conversation_id, role, content, created_at
                FROM dc_assistant_message
                WHERE id = ?
                """,
                (rs, rowNum) -> new ChatMessageItem(
                        rs.getLong("id"),
                        rs.getLong("conversation_id"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                messageId
        );
    }

    public List<ChatMessageItem> findMessages(Long conversationId) {
        return jdbc.query(
                """
                SELECT id, conversation_id, role, content, created_at
                FROM dc_assistant_message
                WHERE conversation_id = ?
                ORDER BY created_at ASC, id ASC
                """,
                (rs, rowNum) -> new ChatMessageItem(
                        rs.getLong("id"),
                        rs.getLong("conversation_id"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                conversationId
        );
    }

    public Long insertRecommendation(
            Long conversationId,
            Long messageId,
            Long productId,
            String productName,
            String brandName,
            String categoryName,
            String coverUrl,
            BigDecimal officialPrice,
            BigDecimal usedMinPrice,
            BigDecimal usedMaxPrice,
            Integer productScore,
            Integer matchScore,
            String reason,
            String riskTip,
            String explainSummary,
            List<String> matchedRequirements,
            List<String> tradeoffNotes,
            List<String> knowledgeEvidence,
            Integer sortOrder
    ) {
        jdbc.update(
                """
                INSERT INTO dc_assistant_recommendation
                  (conversation_id, message_id, product_id, product_name, brand_name, category_name, cover_url,
                   official_price, used_min_price, used_max_price, product_score, match_score, reason, risk_tip,
                   explain_summary, matched_requirements, tradeoff_notes, knowledge_evidence, sort_order)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                conversationId,
                messageId,
                productId,
                productName,
                brandName,
                categoryName,
                coverUrl,
                officialPrice,
                usedMinPrice,
                usedMaxPrice,
                productScore,
                matchScore,
                reason,
                riskTip,
                explainSummary,
                toJson(matchedRequirements),
                toJson(tradeoffNotes),
                toJson(knowledgeEvidence),
                sortOrder
        );
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public AssistantRecommendationItem findRecommendation(Long id) {
        return jdbc.queryForObject(
                recommendationSql("WHERE id = ?"),
                (rs, rowNum) -> mapRecommendation(rs),
                id
        );
    }

    public List<AssistantRecommendationItem> findRecommendationsByConversation(Long conversationId) {
        return jdbc.query(
                recommendationSql("WHERE conversation_id = ? ORDER BY created_at ASC, sort_order ASC, id ASC"),
                (rs, rowNum) -> mapRecommendation(rs),
                conversationId
        );
    }

    public List<AssistantRecommendationItem> findRecommendationsByMessage(Long messageId) {
        return jdbc.query(
                recommendationSql("WHERE message_id = ? ORDER BY sort_order ASC, id ASC"),
                (rs, rowNum) -> mapRecommendation(rs),
                messageId
        );
    }

    private String recommendationSql(String condition) {
        return """
                SELECT id, message_id, product_id, product_name, brand_name, category_name, cover_url,
                       official_price, used_min_price, used_max_price, product_score, match_score, reason, risk_tip,
                       explain_summary, matched_requirements, tradeoff_notes, knowledge_evidence
                FROM dc_assistant_recommendation
                """ + condition;
    }

    private AssistantRecommendationItem mapRecommendation(java.sql.ResultSet rs) throws java.sql.SQLException {
        Long productId = rs.getObject("product_id", Long.class);
        return new AssistantRecommendationItem(
                rs.getLong("id"),
                rs.getLong("message_id"),
                productId,
                rs.getString("product_name"),
                rs.getString("brand_name"),
                rs.getString("category_name"),
                rs.getString("cover_url"),
                rs.getBigDecimal("official_price"),
                rs.getBigDecimal("used_min_price"),
                rs.getBigDecimal("used_max_price"),
                rs.getObject("product_score", Integer.class),
                rs.getObject("match_score", Integer.class),
                rs.getString("reason"),
                rs.getString("risk_tip"),
                rs.getString("explain_summary"),
                productId != null ? "/products/" + productId : null,
                buildMatchTags(
                        rs.getObject("match_score", Integer.class),
                        rs.getObject("product_score", Integer.class),
                        rs.getString("reason"),
                        rs.getBigDecimal("used_min_price"),
                        rs.getBigDecimal("used_max_price")
                ),
                buildCautionTags(rs.getString("risk_tip"), rs.getBigDecimal("used_min_price"), rs.getBigDecimal("used_max_price")),
                buildNextActions(productId, rs.getBigDecimal("official_price"), rs.getBigDecimal("used_min_price"), rs.getBigDecimal("used_max_price")),
                readStringList(rs.getString("matched_requirements")),
                readStringList(rs.getString("tradeoff_notes")),
                readStringList(rs.getString("knowledge_evidence"))
        );
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values != null ? values : List.of());
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private List<String> buildMatchTags(Integer matchScore, Integer productScore, String reason, BigDecimal usedMinPrice, BigDecimal usedMaxPrice) {
        List<String> tags = new ArrayList<>();
        if (matchScore != null) addTag(tags, "匹配度 " + matchScore);
        if (productScore != null) addTag(tags, "产品评分 " + productScore);
        String text = reason == null ? "" : reason;
        if (text.contains("知识库")) addTag(tags, "RAG 命中");
        if (text.contains("预算")) addTag(tags, "预算匹配");
        if (usedMinPrice != null || usedMaxPrice != null) addTag(tags, "有二手参考");
        return limit(tags, 4);
    }

    private List<String> buildCautionTags(String riskTip, BigDecimal usedMinPrice, BigDecimal usedMaxPrice) {
        List<String> tags = new ArrayList<>();
        String text = riskTip == null ? "" : riskTip;
        if (text.contains("电池")) addTag(tags, "电池健康");
        if (text.contains("保修") || text.contains("售后")) addTag(tags, "售后保修");
        if (text.contains("价格") || text.contains("预算")) addTag(tags, "价格核对");
        if (usedMinPrice != null || usedMaxPrice != null) addTag(tags, "二手验机");
        if (tags.isEmpty()) addTag(tags, "购买前核对");
        return limit(tags, 3);
    }

    private List<String> buildNextActions(Long productId, BigDecimal officialPrice, BigDecimal usedMinPrice, BigDecimal usedMaxPrice) {
        List<String> actions = new ArrayList<>();
        if (productId != null) {
            addTag(actions, "查看详情");
            addTag(actions, "加入对比");
        }
        if (officialPrice != null || usedMinPrice != null || usedMaxPrice != null) {
            addTag(actions, "设置价格提醒");
        }
        return actions;
    }

    private void addTag(List<String> tags, String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    private List<String> limit(List<String> tags, int max) {
        return tags.size() > max ? tags.subList(0, max) : tags;
    }
}
