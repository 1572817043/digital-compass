USE digital_compass;

-- 1. 创建品牌表
CREATE TABLE IF NOT EXISTS dc_brand (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  logo_url VARCHAR(500) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dc_brand_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品牌表';

-- 2. 创建产品图片表
CREATE TABLE IF NOT EXISTS dc_product_image (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  image_url VARCHAR(500) NOT NULL,
  image_type VARCHAR(20) NOT NULL DEFAULT 'GALLERY' COMMENT 'MAIN: 主图, GALLERY: 图集',
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_image_product (product_id),
  CONSTRAINT fk_dc_image_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品图片表';

-- 3. 给 dc_product 添加 brand_id 字段
ALTER TABLE dc_product ADD COLUMN brand_id BIGINT UNSIGNED NULL AFTER category_id;

-- 4. 插入品牌数据
INSERT INTO dc_brand (name, sort_order) VALUES
  ('Apple', 10),
  ('Xiaomi', 20),
  ('Huawei', 30),
  ('Honor', 40),
  ('Lenovo', 50)
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order);

-- 5. 回填 dc_product.brand_id
UPDATE dc_product p
  JOIN dc_brand b ON p.brand = b.name
  SET p.brand_id = b.id
  WHERE p.brand_id IS NULL;

-- 6. 删除旧的唯一索引，添加包含 brand_id 的新唯一索引
ALTER TABLE dc_product DROP INDEX uk_dc_product_category_name_model;
ALTER TABLE dc_product ADD UNIQUE KEY uk_dc_product_category_brand_name_model (category_id, brand_id, name, model);
