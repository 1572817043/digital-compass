USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_product_metric (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '产品核心指标ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '产品ID',
  metric_key VARCHAR(60) NOT NULL COMMENT '指标编码，如 processor、battery、weight',
  metric_label VARCHAR(80) NOT NULL COMMENT '指标名称',
  metric_value VARCHAR(255) NOT NULL COMMENT '展示值',
  numeric_value DECIMAL(12,2) NULL COMMENT '数值，用于筛选和排序',
  unit VARCHAR(30) NULL COMMENT '单位',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_metric_product (product_id),
  KEY idx_dc_metric_key_value (metric_key, numeric_value),
  UNIQUE KEY uk_dc_metric_product_key (product_id, metric_key),
  CONSTRAINT fk_dc_metric_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品标准化核心指标表';

