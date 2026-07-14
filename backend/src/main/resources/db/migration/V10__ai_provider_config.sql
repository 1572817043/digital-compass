USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_ai_provider_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  provider_code VARCHAR(50) NOT NULL,
  provider_name VARCHAR(100) NOT NULL,
  protocol_type VARCHAR(50) NOT NULL DEFAULT 'openai-compatible',
  base_url VARCHAR(500) NOT NULL,
  api_key_cipher VARCHAR(500) NULL,
  chat_model VARCHAR(100) NULL,
  embedding_model VARCHAR(100) NULL,
  temperature DOUBLE NULL DEFAULT 0.7,
  max_tokens INT NULL DEFAULT 2048,
  timeout_seconds INT NULL DEFAULT 30,
  enabled TINYINT NOT NULL DEFAULT 1,
  default_provider TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_code (provider_code),
  KEY idx_ai_provider_protocol (protocol_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI供应商配置表';

CREATE TABLE IF NOT EXISTS dc_ai_workflow_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NULL,
  conversation_id BIGINT UNSIGNED NULL,
  provider_id BIGINT UNSIGNED NULL,
  model_name VARCHAR(100) NULL,
  user_requirement TEXT NULL,
  parsed_requirement_json TEXT NULL,
  retrieved_context_summary TEXT NULL,
  candidate_product_ids VARCHAR(500) NULL,
  fallback_used TINYINT NOT NULL DEFAULT 0,
  error_message VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_workflow_user (user_id),
  KEY idx_workflow_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI工作流日志表';
