package com.a0000.digicompass.modules.ai.knowledge.mapper;

import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeChunkItem;
import com.a0000.digicompass.modules.ai.knowledge.dto.AiKnowledgeSearchResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiKnowledgeChunkMapper {

    private final JdbcTemplate jdbc;

    public AiKnowledgeChunkMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AiKnowledgeChunkItem> findByKnowledgeId(Long knowledgeId) {
        return jdbc.query(
                "SELECT id, knowledge_id, product_id, category_id, chunk_index, title, content, char_count, status FROM dc_ai_knowledge_chunk WHERE knowledge_id = ? ORDER BY chunk_index",
                (rs, rn) -> new AiKnowledgeChunkItem(
                        rs.getLong("id"), rs.getObject("knowledge_id") != null ? rs.getLong("knowledge_id") : null,
                        rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                        rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                        rs.getInt("chunk_index"), rs.getString("title"), rs.getString("content"),
                        rs.getInt("char_count"), rs.getInt("status")
                ), knowledgeId);
    }

    public void deleteByKnowledgeId(Long knowledgeId) {
        jdbc.update("DELETE FROM dc_ai_knowledge_chunk WHERE knowledge_id = ?", knowledgeId);
    }

    public void insert(Long knowledgeId, Long productId, Long categoryId, int chunkIndex, String title, String content, String contentHash, int charCount) {
        jdbc.update("INSERT INTO dc_ai_knowledge_chunk (knowledge_id, product_id, category_id, chunk_index, title, content, content_hash, char_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                knowledgeId, productId, categoryId, chunkIndex, title, content, contentHash, charCount);
    }

    public List<AiKnowledgeSearchResult> search(String query, Long categoryId, Long productId, int limit) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isBlank()) {
            return List.of();
        }
        List<String> keywords = extractKeywords(normalizedQuery);
        StringBuilder sql = new StringBuilder("""
                SELECT id AS chunk_id, knowledge_id, product_id, category_id, title, content
                FROM dc_ai_knowledge_chunk
                WHERE status = 1
                """);
        List<Object> params = new ArrayList<>();

        if (productId != null) {
            sql.append(" AND product_id = ?");
            params.add(productId);
        }
        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        sql.append(" ORDER BY updated_at DESC, id DESC LIMIT ?");
        params.add(Math.max(limit * 20, 100));

        return jdbc.query(sql.toString(), (rs, rn) -> new ChunkRow(
                        rs.getLong("chunk_id"),
                        rs.getObject("knowledge_id") != null ? rs.getLong("knowledge_id") : null,
                        rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                        rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                        rs.getString("title"),
                        rs.getString("content")
                ), params.toArray())
                .stream()
                .map(row -> toSearchResult(row, normalizedQuery, keywords, categoryId, productId))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(AiKnowledgeSearchResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private AiKnowledgeSearchResult toSearchResult(ChunkRow row, String query, List<String> keywords, Long categoryId, Long productId) {
        String title = normalize(row.title());
        String content = normalize(row.content());
        int score = 0;
        if (!query.isBlank() && title.contains(query)) score += 20;
        if (!query.isBlank() && content.contains(query)) score += 10;
        for (String keyword : keywords) {
            if (title.contains(keyword)) score += 20;
            if (content.contains(keyword)) score += 10;
        }
        if (productId != null && Objects.equals(productId, row.productId())) score += 20;
        if (categoryId != null && Objects.equals(categoryId, row.categoryId())) score += 10;
        return new AiKnowledgeSearchResult(
                row.chunkId(),
                row.knowledgeId(),
                row.productId(),
                row.title(),
                row.content(),
                score,
                "KEYWORD",
                null
        );
    }

    private List<String> extractKeywords(String query) {
        Set<String> keywords = new LinkedHashSet<>();
        Pattern.compile("[,，、。；;\\s]+")
                .splitAsStream(query)
                .map(String::trim)
                .filter(item -> item.length() >= 2)
                .forEach(keywords::add);

        for (String word : List.of("手机", "电脑", "笔记本", "平板", "耳机", "手表", "拍照", "影像", "旅行", "游戏", "办公", "学习", "轻薄", "续航", "二手", "预算")) {
            if (query.contains(word)) {
                keywords.add(word);
            }
        }

        Matcher matcher = Pattern.compile("\\d{3,6}").matcher(query);
        while (matcher.find()) {
            keywords.add(matcher.group());
        }
        return new ArrayList<>(keywords);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record ChunkRow(
            Long chunkId,
            Long knowledgeId,
            Long productId,
            Long categoryId,
            String title,
            String content
    ) {
    }
}
