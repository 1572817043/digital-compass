USE digital_compass;

ALTER TABLE dc_ai_provider_config
  ADD COLUMN protocol_type VARCHAR(50) NOT NULL DEFAULT 'openai-compatible' AFTER provider_name;

UPDATE dc_ai_provider_config
SET protocol_type = 'openai-compatible'
WHERE protocol_type IS NULL OR protocol_type = '';
