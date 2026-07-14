package com.a0000.digicompass.modules.ai.knowledge.mapper;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeItem;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiKnowledgeMapper {

    private final JdbcTemplate jdbc;

    public AiKnowledgeMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AiKnowledgeItem> findAll(String keyword, String knowledgeType, Integer status, Long categoryId, Long productId) {
        StringBuilder sql = new StringBuilder("SELECT id, category_id, product_id, title, content, knowledge_type, tags, source, status FROM dc_ai_knowledge WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like); params.add(like);
        }
        if (knowledgeType != null && !knowledgeType.isBlank()) { sql.append(" AND knowledge_type = ?"); params.add(knowledgeType); }
        if (status != null) { sql.append(" AND status = ?"); params.add(status); }
        if (categoryId != null) { sql.append(" AND category_id = ?"); params.add(categoryId); }
        if (productId != null) { sql.append(" AND product_id = ?"); params.add(productId); }
        sql.append(" ORDER BY id DESC");
        return jdbc.query(sql.toString(), (rs, rn) -> new AiKnowledgeItem(
                rs.getLong("id"), rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                rs.getString("title"), rs.getString("content"), rs.getString("knowledge_type"),
                rs.getString("tags"), rs.getString("source"), rs.getInt("status")
        ), params.toArray());
    }

    public AiKnowledgeItem findById(Long id) {
        List<AiKnowledgeItem> results = jdbc.query(
                "SELECT id, category_id, product_id, title, content, knowledge_type, tags, source, status FROM dc_ai_knowledge WHERE id = ?",
                (rs, rn) -> new AiKnowledgeItem(
                        rs.getLong("id"), rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                        rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                        rs.getString("title"), rs.getString("content"), rs.getString("knowledge_type"),
                        rs.getString("tags"), rs.getString("source"), rs.getInt("status")
                ), id);
        return results.isEmpty() ? null : results.getFirst();
    }

    public AiKnowledgeItem findByProductId(Long productId) {
        List<AiKnowledgeItem> results = jdbc.query(
                "SELECT id, category_id, product_id, title, content, knowledge_type, tags, source, status FROM dc_ai_knowledge WHERE product_id = ? AND knowledge_type = 'product' LIMIT 1",
                (rs, rn) -> new AiKnowledgeItem(
                        rs.getLong("id"), rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                        rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                        rs.getString("title"), rs.getString("content"), rs.getString("knowledge_type"),
                        rs.getString("tags"), rs.getString("source"), rs.getInt("status")
                ), productId);
        return results.isEmpty() ? null : results.getFirst();
    }

    public Long insert(Long categoryId, Long productId, String title, String content, String knowledgeType, String tags, String source, int status) {
        jdbc.update("INSERT INTO dc_ai_knowledge (category_id, product_id, title, content, knowledge_type, tags, source, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                categoryId, productId, title, content, knowledgeType, tags, source, status);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Long id, Long categoryId, Long productId, String title, String content, String knowledgeType, String tags, String source, int status) {
        jdbc.update("UPDATE dc_ai_knowledge SET category_id=?, product_id=?, title=?, content=?, knowledge_type=?, tags=?, source=?, status=? WHERE id=?",
                categoryId, productId, title, content, knowledgeType, tags, source, status, id);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM dc_ai_knowledge WHERE id = ?", id);
    }
}
