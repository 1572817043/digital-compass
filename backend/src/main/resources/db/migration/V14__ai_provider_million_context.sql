USE digital_compass;

ALTER TABLE dc_ai_provider_config
  ADD COLUMN million_context TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持1M上下文：1支持 0不支持' AFTER max_tokens;
