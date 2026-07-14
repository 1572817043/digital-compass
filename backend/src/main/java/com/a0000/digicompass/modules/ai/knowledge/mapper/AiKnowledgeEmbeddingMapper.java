package com.a0000.digicompass.modules.ai.knowledge.mapper;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiKnowledgeEmbeddingMapper {

    private final JdbcTemplate jdbc;

    public AiKnowledgeEmbeddingMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ChunkForEmbedding> findActiveChunks(Long knowledgeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.knowledge_id, c.product_id, c.category_id, c.title, c.content, c.content_hash
                FROM dc_ai_knowledge_chunk c
                JOIN dc_ai_knowledge k ON c.knowledge_id = k.id
                WHERE c.status = 1 AND k.status = 1
                """);
        if (knowledgeId != null) {
            sql.append(" AND c.knowledge_id = ? ");
            sql.append(" ORDER BY c.id");
            return jdbc.query(sql.toString(), (rs, rn) -> new ChunkForEmbedding(
                    rs.getLong("id"),
                    rs.getObject("knowledge_id") != null ? rs.getLong("knowledge_id") : null,
                    rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                    rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("content_hash")
            ), knowledgeId);
        }
        sql.append(" ORDER BY c.id");
        return jdbc.query(sql.toString(), (rs, rn) -> new ChunkForEmbedding(
                rs.getLong("id"),
                rs.getObject("knowledge_id") != null ? rs.getLong("knowledge_id") : null,
                rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("content_hash")
        ));
    }

    public void upsertEmbedding(Long chunkId, Long providerId, String modelName, String contentHash, String vectorJson, int dimension) {
        jdbc.update("""
                INSERT INTO dc_ai_knowledge_embedding
                  (chunk_id, provider_id, model_name, content_hash, vector_json, dimension, status)
                VALUES (?, ?, ?, ?, ?, ?, 1)
                ON DUPLICATE KEY UPDATE
                  provider_id = VALUES(provider_id),
                  model_name = VALUES(model_name),
                  content_hash = VALUES(content_hash),
                  vector_json = VALUES(vector_json),
                  dimension = VALUES(dimension),
                  status = 1
                """, chunkId, providerId, modelName, contentHash, vectorJson, dimension);
    }

    public List<EmbeddingRow> findSearchableEmbeddings(Long categoryId, Long productId) {
        StringBuilder sql = new StringBuilder("""
                SELECT e.chunk_id, c.knowledge_id, c.product_id, c.category_id, c.title, c.content,
                       e.vector_json, e.dimension, e.model_name
                FROM dc_ai_knowledge_embedding e
                JOIN dc_ai_knowledge_chunk c ON e.chunk_id = c.id
                JOIN dc_ai_knowledge k ON c.knowledge_id = k.id
                WHERE e.status = 1 AND c.status = 1 AND k.status = 1
                """);
        java.util.ArrayList<Object> params = new java.util.ArrayList<>();
        if (categoryId != null) {
            sql.append(" AND c.category_id = ?");
            params.add(categoryId);
        }
        if (productId != null) {
            sql.append(" AND c.product_id = ?");
            params.add(productId);
        }
        return jdbc.query(sql.toString(), (rs, rn) -> new EmbeddingRow(
                rs.getLong("chunk_id"),
                rs.getObject("knowledge_id") != null ? rs.getLong("knowledge_id") : null,
                rs.getObject("product_id") != null ? rs.getLong("product_id") : null,
                rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("vector_json"),
                rs.getInt("dimension"),
                rs.getString("model_name")
        ), params.toArray());
    }

    public record ChunkForEmbedding(
            Long id,
            Long knowledgeId,
            Long productId,
            Long categoryId,
            String title,
            String content,
            String contentHash
    ) {
    }

    public record EmbeddingRow(
            Long chunkId,
            Long knowledgeId,
            Long productId,
            Long categoryId,
            String title,
            String content,
            String vectorJson,
            int dimension,
            String modelName
    ) {
    }
}
