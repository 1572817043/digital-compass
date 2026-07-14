CREATE TABLE IF NOT EXISTS dc_assistant_conversation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '助手会话ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  title VARCHAR(120) NOT NULL DEFAULT '新的选购咨询' COMMENT '会话标题',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_assistant_conversation_user_updated (user_id, updated_at),
  CONSTRAINT fk_dc_assistant_conversation_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选购助手会话表';

CREATE TABLE IF NOT EXISTS dc_assistant_message (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  conversation_id BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  role VARCHAR(20) NOT NULL COMMENT '消息角色：USER、ASSISTANT',
  content TEXT NOT NULL COMMENT '消息内容',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_assistant_message_conversation_created (conversation_id, created_at),
  KEY idx_dc_assistant_message_user (user_id),
  CONSTRAINT fk_dc_assistant_message_conversation FOREIGN KEY (conversation_id) REFERENCES dc_assistant_conversation (id) ON DELETE CASCADE,
  CONSTRAINT fk_dc_assistant_message_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选购助手消息表';

CREATE TABLE IF NOT EXISTS dc_assistant_recommendation (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '推荐结果ID',
  conversation_id BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  message_id BIGINT UNSIGNED NOT NULL COMMENT '助手消息ID',
  product_id BIGINT UNSIGNED NULL COMMENT '产品ID',
  product_name VARCHAR(160) NOT NULL COMMENT '推荐时产品名称',
  brand_name VARCHAR(120) NULL COMMENT '推荐时品牌名称',
  category_name VARCHAR(80) NULL COMMENT '推荐时分类名称',
  cover_url VARCHAR(800) NULL COMMENT '推荐时封面图',
  official_price DECIMAL(10,2) NULL COMMENT '推荐时官方价',
  used_min_price DECIMAL(10,2) NULL COMMENT '推荐时二手最低价',
  used_max_price DECIMAL(10,2) NULL COMMENT '推荐时二手最高价',
  product_score INT NULL COMMENT '产品库评分',
  match_score INT NULL COMMENT '本次需求匹配分',
  reason TEXT NULL COMMENT '推荐理由',
  risk_tip TEXT NULL COMMENT '风险提示',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_assistant_rec_conversation (conversation_id),
  KEY idx_dc_assistant_rec_message (message_id),
  KEY idx_dc_assistant_rec_product (product_id),
  CONSTRAINT fk_dc_assistant_rec_conversation FOREIGN KEY (conversation_id) REFERENCES dc_assistant_conversation (id) ON DELETE CASCADE,
  CONSTRAINT fk_dc_assistant_rec_message FOREIGN KEY (message_id) REFERENCES dc_assistant_message (id) ON DELETE CASCADE,
  CONSTRAINT fk_dc_assistant_rec_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选购助手推荐结果表';
