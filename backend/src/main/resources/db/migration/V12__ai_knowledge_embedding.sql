USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_ai_knowledge_embedding (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  chunk_id BIGINT UNSIGNED NOT NULL,
  provider_id BIGINT UNSIGNED NULL,
  model_name VARCHAR(120) NOT NULL,
  content_hash VARCHAR(64) NULL,
  vector_json MEDIUMTEXT NOT NULL,
  dimension INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_embedding_chunk (chunk_id),
  KEY idx_embedding_provider_model (provider_id, model_name),
  KEY idx_embedding_status (status),
  CONSTRAINT fk_embedding_chunk FOREIGN KEY (chunk_id) REFERENCES dc_ai_knowledge_chunk (id) ON DELETE CASCADE,
  CONSTRAINT fk_embedding_provider FOREIGN KEY (provider_id) REFERENCES dc_ai_provider_config (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识向量表';
