USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_price_alert (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  target_price DECIMAL(10,2) NOT NULL,
  price_type VARCHAR(30) NOT NULL DEFAULT 'official',
  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  last_price DECIMAL(10,2) NULL,
  triggered_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product_price_type (user_id, product_id, price_type),
  KEY idx_alert_user (user_id),
  KEY idx_alert_product (product_id),
  CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='价格提醒表';
