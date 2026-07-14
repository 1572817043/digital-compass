USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_user_view_history (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  viewed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product (user_id, product_id),
  KEY idx_view_user (user_id),
  KEY idx_view_product (product_id),
  CONSTRAINT fk_view_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_view_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户浏览历史表';
