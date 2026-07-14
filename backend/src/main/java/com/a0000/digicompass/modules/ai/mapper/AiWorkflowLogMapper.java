package com.a0000.digicompass.modules.ai.mapper;

import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowCandidateProduct;
import com.a0000.digicompass.modules.ai.workflow.dto.AiWorkflowLogItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiWorkflowLogMapper {

    private final JdbcTemplate jdbc;

    public AiWorkflowLogMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(
            Long userId,
            Long conversationId,
            Long providerId,
            String modelName,
            String userRequirement,
            String parsedRequirementJson,
            String retrievedContextSummary,
            String candidateProductIds,
            boolean fallbackUsed,
            String errorMessage
    ) {
        jdbc.update(
                """
                INSERT INTO dc_ai_workflow_log
                  (user_id, conversation_id, provider_id, model_name, user_requirement,
                   parsed_requirement_json, retrieved_context_summary, candidate_product_ids,
                   fallback_used, error_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userId,
                conversationId,
                providerId,
                modelName,
                userRequirement,
                parsedRequirementJson,
                retrievedContextSummary,
                candidateProductIds,
                fallbackUsed ? 1 : 0,
                errorMessage
        );
    }

    public List<AiWorkflowLogItem> findRecent(String keyword, Boolean fallbackUsed, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT l.id, l.user_id, u.username, l.conversation_id,
                       l.provider_id, p.provider_name, l.model_name, l.user_requirement,
                       l.parsed_requirement_json, l.retrieved_context_summary,
                       l.candidate_product_ids, l.fallback_used, l.error_message, l.created_at
                FROM dc_ai_workflow_log l
                LEFT JOIN dc_user u ON u.id = l.user_id
                LEFT JOIN dc_ai_provider_config p ON p.id = l.provider_id
                WHERE 1 = 1
                """);
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("""
                    AND (
                      l.user_requirement LIKE ?
                      OR l.parsed_requirement_json LIKE ?
                      OR l.retrieved_context_summary LIKE ?
                      OR l.candidate_product_ids LIKE ?
                    )
                    """);
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (fallbackUsed != null) {
            sql.append("AND l.fallback_used = ?\n");
            args.add(fallbackUsed ? 1 : 0);
        }
        sql.append("ORDER BY l.id DESC LIMIT ?");
        args.add(Math.max(1, Math.min(limit, 200)));

        return jdbc.query(sql.toString(), (rs, rowNum) -> new AiWorkflowLogItem(
                rs.getLong("id"),
                rs.getObject("user_id", Long.class),
                rs.getString("username"),
                rs.getObject("conversation_id", Long.class),
                rs.getObject("provider_id", Long.class),
                rs.getString("provider_name"),
                rs.getString("model_name"),
                rs.getString("user_requirement"),
                rs.getString("parsed_requirement_json"),
                rs.getString("retrieved_context_summary"),
                rs.getString("candidate_product_ids"),
                rs.getInt("fallback_used") == 1,
                rs.getString("error_message"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), args.toArray());
    }

    public AiWorkflowLogItem findById(Long id) {
        List<AiWorkflowLogItem> results = jdbc.query("""
                SELECT l.id, l.user_id, u.username, l.conversation_id,
                       l.provider_id, p.provider_name, l.model_name, l.user_requirement,
                       l.parsed_requirement_json, l.retrieved_context_summary,
                       l.candidate_product_ids, l.fallback_used, l.error_message, l.created_at
                FROM dc_ai_workflow_log l
                LEFT JOIN dc_user u ON u.id = l.user_id
                LEFT JOIN dc_ai_provider_config p ON p.id = l.provider_id
                WHERE l.id = ?
                """, (rs, rowNum) -> new AiWorkflowLogItem(
                rs.getLong("id"),
                rs.getObject("user_id", Long.class),
                rs.getString("username"),
                rs.getObject("conversation_id", Long.class),
                rs.getObject("provider_id", Long.class),
                rs.getString("provider_name"),
                rs.getString("model_name"),
                rs.getString("user_requirement"),
                rs.getString("parsed_requirement_json"),
                rs.getString("retrieved_context_summary"),
                rs.getString("candidate_product_ids"),
                rs.getInt("fallback_used") == 1,
                rs.getString("error_message"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), id);
        return results.isEmpty() ? null : results.getFirst();
    }

    public List<AiWorkflowCandidateProduct> findCandidateProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        List<AiWorkflowCandidateProduct> products = jdbc.query("""
                SELECT p.id, p.name, p.brand, c.name AS category_name, p.cover_url, p.official_price, p.score
                FROM dc_product p
                LEFT JOIN dc_category c ON c.id = p.category_id
                WHERE p.id IN (%s)
                """.formatted(placeholders), (rs, rowNum) -> new AiWorkflowCandidateProduct(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("brand"),
                rs.getString("category_name"),
                rs.getString("cover_url"),
                rs.getBigDecimal("official_price"),
                rs.getObject("score", Integer.class),
                "/products/" + rs.getLong("id")
        ), ids.toArray());
        Map<Long, AiWorkflowCandidateProduct> byId = new LinkedHashMap<>();
        for (AiWorkflowCandidateProduct product : products) {
            byId.put(product.id(), product);
        }
        return ids.stream()
                .map(byId::get)
                .filter(item -> item != null)
                .toList();
    }
}
