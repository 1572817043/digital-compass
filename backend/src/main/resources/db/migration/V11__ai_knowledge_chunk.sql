USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_ai_knowledge_chunk (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  knowledge_id BIGINT UNSIGNED NULL,
  product_id BIGINT UNSIGNED NULL,
  category_id BIGINT UNSIGNED NULL,
  chunk_index INT NOT NULL DEFAULT 0,
  title VARCHAR(200) NULL,
  content TEXT NOT NULL,
  content_hash VARCHAR(64) NULL,
  char_count INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_chunk_knowledge (knowledge_id),
  KEY idx_chunk_product (product_id),
  KEY idx_chunk_category (category_id),
  KEY idx_chunk_status (status),
  CONSTRAINT fk_chunk_knowledge FOREIGN KEY (knowledge_id) REFERENCES dc_ai_knowledge (id) ON DELETE SET NULL,
  CONSTRAINT fk_chunk_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识切片表';
