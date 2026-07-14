USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_product_tag (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '产品标签ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '产品ID',
  tag_type VARCHAR(40) NOT NULL COMMENT '标签类型：selling_point、weakness、suitable、unsuitable、scene',
  tag_name VARCHAR(80) NOT NULL COMMENT '标签名称',
  tag_value VARCHAR(500) NULL COMMENT '补充说明',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_tag_product (product_id),
  KEY idx_dc_tag_product_type (product_id, tag_type),
  UNIQUE KEY uk_dc_tag_product_type_name (product_id, tag_type, tag_name),
  CONSTRAINT fk_dc_tag_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品选购标签表';

