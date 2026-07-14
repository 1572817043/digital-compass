package com.a0000.digicompass.modules.ai.mapper;

import com.a0000.digicompass.modules.ai.dto.AiProviderConfigItem;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AiProviderConfigMapper {

    private final JdbcTemplate jdbc;

    public AiProviderConfigMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<AiProviderConfigItem> findAll() {
        return jdbc.query("""
                SELECT id, provider_code, provider_name, protocol_type, base_url, api_key_cipher,
                       chat_model, embedding_model, temperature, max_tokens, million_context, timeout_seconds,
                       enabled, default_provider, remark
                FROM dc_ai_provider_config ORDER BY id
                """, (rs, rn) -> new AiProviderConfigItem(
                rs.getLong("id"), rs.getString("provider_code"), rs.getString("provider_name"),
                rs.getString("protocol_type"), rs.getString("base_url"), rs.getString("api_key_cipher"),
                rs.getString("chat_model"), rs.getString("embedding_model"),
                rs.getDouble("temperature"), rs.getInt("max_tokens"), rs.getInt("million_context") == 1, rs.getInt("timeout_seconds"),
                rs.getInt("enabled") == 1, rs.getInt("default_provider") == 1, rs.getString("remark")
        ));
    }

    public AiProviderConfigItem findById(Long id) {
        List<AiProviderConfigItem> results = jdbc.query("""
                SELECT id, provider_code, provider_name, protocol_type, base_url, api_key_cipher,
                       chat_model, embedding_model, temperature, max_tokens, million_context, timeout_seconds,
                       enabled, default_provider, remark
                FROM dc_ai_provider_config WHERE id = ?
                """,
                (rs, rn) -> new AiProviderConfigItem(
                        rs.getLong("id"), rs.getString("provider_code"), rs.getString("provider_name"),
                        rs.getString("protocol_type"), rs.getString("base_url"), rs.getString("api_key_cipher"),
                        rs.getString("chat_model"), rs.getString("embedding_model"),
                        rs.getDouble("temperature"), rs.getInt("max_tokens"), rs.getInt("million_context") == 1, rs.getInt("timeout_seconds"),
                        rs.getInt("enabled") == 1, rs.getInt("default_provider") == 1, rs.getString("remark")
                ), id);
        return results.isEmpty() ? null : results.getFirst();
    }

    public AiProviderConfigItem findDefault() {
        List<AiProviderConfigItem> results = jdbc.query("""
                SELECT id, provider_code, provider_name, protocol_type, base_url, api_key_cipher,
                       chat_model, embedding_model, temperature, max_tokens, million_context, timeout_seconds,
                       enabled, default_provider, remark
                FROM dc_ai_provider_config WHERE enabled = 1 AND default_provider = 1 LIMIT 1
                """, (rs, rn) -> new AiProviderConfigItem(
                rs.getLong("id"), rs.getString("provider_code"), rs.getString("provider_name"),
                rs.getString("protocol_type"), rs.getString("base_url"), rs.getString("api_key_cipher"),
                rs.getString("chat_model"), rs.getString("embedding_model"),
                rs.getDouble("temperature"), rs.getInt("max_tokens"), rs.getInt("million_context") == 1, rs.getInt("timeout_seconds"),
                rs.getInt("enabled") == 1, rs.getInt("default_provider") == 1, rs.getString("remark")
        ));
        return results.isEmpty() ? null : results.getFirst();
    }

    public Long insert(String providerCode, String providerName, String protocolType, String baseUrl, String apiKeyCipher,
                       String chatModel, String embeddingModel, Double temperature, Integer maxTokens,
                       boolean millionContext, Integer timeoutSeconds, boolean enabled, boolean defaultProvider, String remark) {
        jdbc.update("""
                INSERT INTO dc_ai_provider_config
                (provider_code, provider_name, protocol_type, base_url, api_key_cipher, chat_model, embedding_model,
                 temperature, max_tokens, million_context, timeout_seconds, enabled, default_provider, remark)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, providerCode, providerName, protocolType, baseUrl, apiKeyCipher, chatModel, embeddingModel,
                temperature, maxTokens, millionContext ? 1 : 0, timeoutSeconds, enabled ? 1 : 0, defaultProvider ? 1 : 0, remark);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void update(Long id, String providerCode, String providerName, String protocolType, String baseUrl, String apiKeyCipher,
                       String chatModel, String embeddingModel, Double temperature, Integer maxTokens,
                       boolean millionContext, Integer timeoutSeconds, boolean enabled, boolean defaultProvider, String remark) {
        jdbc.update("""
                UPDATE dc_ai_provider_config
                SET provider_code=?, provider_name=?, protocol_type=?, base_url=?, api_key_cipher=?, chat_model=?, embedding_model=?,
                    temperature=?, max_tokens=?, million_context=?, timeout_seconds=?, enabled=?, default_provider=?, remark=?
                WHERE id=?
                """, providerCode, providerName, protocolType, baseUrl, apiKeyCipher, chatModel, embeddingModel,
                temperature, maxTokens, millionContext ? 1 : 0, timeoutSeconds, enabled ? 1 : 0, defaultProvider ? 1 : 0, remark, id);
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM dc_ai_provider_config WHERE id = ?", id);
    }

    public void clearDefault() {
        jdbc.update("UPDATE dc_ai_provider_config SET default_provider = 0");
    }

    public void setDefault(Long id) {
        jdbc.update("UPDATE dc_ai_provider_config SET default_provider = 1 WHERE id = ?", id);
    }
}
