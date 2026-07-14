CREATE DATABASE IF NOT EXISTS digital_compass
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE digital_compass;

CREATE TABLE IF NOT EXISTS dc_category (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  code VARCHAR(50) NOT NULL COMMENT '分类编码，如 phone、laptop',
  name VARCHAR(80) NOT NULL COMMENT '分类名称',
  description VARCHAR(255) NULL COMMENT '分类说明',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dc_category_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品分类表';

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

CREATE TABLE IF NOT EXISTS dc_product (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  category_id BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  brand_id BIGINT UNSIGNED NULL,
  brand VARCHAR(80) NOT NULL COMMENT '品牌',
  name VARCHAR(120) NOT NULL COMMENT '产品名称',
  model VARCHAR(120) NULL COMMENT '型号',
  cover_url VARCHAR(500) NULL COMMENT '封面图地址',
  summary VARCHAR(500) NULL COMMENT '简介',
  official_price DECIMAL(10,2) NULL COMMENT '官方起售价',
  release_date DATE NULL COMMENT '发售日期',
  score INT NOT NULL DEFAULT 0 COMMENT '站内推荐分',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_product_category (category_id),
  KEY idx_dc_product_brand (brand),
  KEY idx_dc_product_score (score),
  UNIQUE KEY uk_dc_product_category_brand_name_model (category_id, brand_id, name, model),
  CONSTRAINT fk_dc_product_category FOREIGN KEY (category_id) REFERENCES dc_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品主表';

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

CREATE TABLE IF NOT EXISTS dc_product_spec (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '参数ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '产品ID',
  spec_group VARCHAR(80) NOT NULL COMMENT '参数分组，如性能、屏幕、续航',
  spec_name VARCHAR(80) NOT NULL COMMENT '参数名',
  spec_value VARCHAR(500) NOT NULL COMMENT '参数值',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_spec_product (product_id),
  UNIQUE KEY uk_dc_spec_product_name (product_id, spec_group, spec_name),
  CONSTRAINT fk_dc_spec_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品动态参数表';

CREATE TABLE IF NOT EXISTS dc_price_reference (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '价格参考ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '产品ID',
  price_type VARCHAR(30) NOT NULL COMMENT '价格类型：official、used、channel',
  platform VARCHAR(80) NOT NULL COMMENT '平台或来源',
  min_price DECIMAL(10,2) NULL COMMENT '最低参考价',
  max_price DECIMAL(10,2) NULL COMMENT '最高参考价',
  avg_price DECIMAL(10,2) NULL COMMENT '均价',
  sample_count INT NOT NULL DEFAULT 0 COMMENT '样本数量',
  reference_date DATE NOT NULL COMMENT '参考日期',
  source_type VARCHAR(30) NOT NULL DEFAULT 'manual' COMMENT '来源类型：manual、import、model',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_price_product (product_id),
  KEY idx_dc_price_type_date (price_type, reference_date),
  UNIQUE KEY uk_dc_price_snapshot (product_id, price_type, platform, reference_date),
  CONSTRAINT fk_dc_price_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='价格参考表';

CREATE TABLE IF NOT EXISTS dc_purchase_link (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购买链接ID',
  product_id BIGINT UNSIGNED NOT NULL COMMENT '产品ID',
  platform VARCHAR(80) NOT NULL COMMENT '平台：官网、京东、天猫等',
  link_type VARCHAR(30) NOT NULL DEFAULT 'official' COMMENT '链接类型：official、store、used_search',
  title VARCHAR(120) NOT NULL COMMENT '链接标题',
  url VARCHAR(1000) NOT NULL COMMENT '跳转地址',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_link_product (product_id),
  UNIQUE KEY uk_dc_link_product_platform (product_id, platform, link_type, title),
  CONSTRAINT fk_dc_link_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购买链接表';

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

CREATE TABLE IF NOT EXISTS dc_ai_knowledge (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  category_id BIGINT UNSIGNED NULL COMMENT '关联分类ID',
  product_id BIGINT UNSIGNED NULL COMMENT '关联产品ID',
  title VARCHAR(160) NOT NULL COMMENT '知识标题',
  content TEXT NOT NULL COMMENT '知识内容',
  knowledge_type VARCHAR(40) NOT NULL COMMENT '知识类型：guide、risk、review、rule',
  tags VARCHAR(255) NULL COMMENT '标签，逗号分隔',
  source VARCHAR(255) NULL COMMENT '来源说明',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_knowledge_category (category_id),
  KEY idx_dc_knowledge_product (product_id),
  KEY idx_dc_knowledge_type (knowledge_type),
  UNIQUE KEY uk_dc_knowledge_title (title),
  CONSTRAINT fk_dc_knowledge_category FOREIGN KEY (category_id) REFERENCES dc_category (id) ON DELETE SET NULL,
  CONSTRAINT fk_dc_knowledge_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识库表';

CREATE TABLE IF NOT EXISTS dc_ai_knowledge_chunk (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识切片ID',
  knowledge_id BIGINT UNSIGNED NULL COMMENT '知识ID',
  product_id BIGINT UNSIGNED NULL COMMENT '关联产品ID',
  category_id BIGINT UNSIGNED NULL COMMENT '关联分类ID',
  chunk_index INT NOT NULL DEFAULT 0 COMMENT '切片序号',
  title VARCHAR(200) NULL COMMENT '切片标题',
  content TEXT NOT NULL COMMENT '切片内容',
  content_hash VARCHAR(64) NULL COMMENT '内容哈希',
  char_count INT NOT NULL DEFAULT 0 COMMENT '字符数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
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

CREATE TABLE IF NOT EXISTS dc_ai_knowledge_embedding (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知识向量ID',
  chunk_id BIGINT UNSIGNED NOT NULL COMMENT '知识切片ID',
  provider_id BIGINT UNSIGNED NULL COMMENT 'AI供应商配置ID',
  model_name VARCHAR(120) NOT NULL COMMENT '向量模型名称',
  content_hash VARCHAR(64) NULL COMMENT '切片内容哈希',
  vector_json MEDIUMTEXT NOT NULL COMMENT '向量JSON数组',
  dimension INT NOT NULL COMMENT '向量维度',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_embedding_chunk (chunk_id),
  KEY idx_embedding_provider_model (provider_id, model_name),
  KEY idx_embedding_status (status),
  CONSTRAINT fk_embedding_chunk FOREIGN KEY (chunk_id) REFERENCES dc_ai_knowledge_chunk (id) ON DELETE CASCADE,
  CONSTRAINT fk_embedding_provider FOREIGN KEY (provider_id) REFERENCES dc_ai_provider_config (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识向量表';

CREATE TABLE IF NOT EXISTS dc_ai_provider_config (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'AI供应商配置ID',
  provider_code VARCHAR(50) NOT NULL COMMENT '供应商编码：deepseek、mimo、openai-compatible、custom',
  provider_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
  protocol_type VARCHAR(50) NOT NULL DEFAULT 'openai-compatible' COMMENT '协议类型：openai-compatible、anthropic-compatible、custom',
  base_url VARCHAR(500) NOT NULL COMMENT '接口基础地址',
  api_key_cipher VARCHAR(500) NULL COMMENT '加密后的API Key',
  chat_model VARCHAR(100) NULL COMMENT '聊天模型名称',
  embedding_model VARCHAR(100) NULL COMMENT '向量模型名称',
  temperature DOUBLE NULL DEFAULT 0.7 COMMENT '生成温度',
  max_tokens INT NULL DEFAULT 2048 COMMENT '最大输出Token',
  million_context TINYINT NOT NULL DEFAULT 0 COMMENT '是否支持1M上下文：1支持 0不支持',
  timeout_seconds INT NULL DEFAULT 30 COMMENT '调用超时时间',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
  default_provider TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认供应商：1是 0否',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_code (provider_code),
  KEY idx_ai_provider_protocol (protocol_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI供应商配置表';

CREATE TABLE IF NOT EXISTS dc_ai_workflow_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'AI工作流日志ID',
  user_id BIGINT UNSIGNED NULL COMMENT '用户ID',
  conversation_id BIGINT UNSIGNED NULL COMMENT '会话ID',
  provider_id BIGINT UNSIGNED NULL COMMENT 'AI供应商配置ID',
  model_name VARCHAR(100) NULL COMMENT '模型名称',
  user_requirement TEXT NULL COMMENT '用户原始需求',
  parsed_requirement_json TEXT NULL COMMENT '需求解析JSON',
  retrieved_context_summary TEXT NULL COMMENT 'RAG检索摘要，第一阶段预留',
  candidate_product_ids VARCHAR(500) NULL COMMENT '候选产品ID列表',
  fallback_used TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用规则回退',
  error_message VARCHAR(1000) NULL COMMENT '错误或回退说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_workflow_user (user_id),
  KEY idx_workflow_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI工作流日志表';

CREATE TABLE IF NOT EXISTS dc_user (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(80) NOT NULL COMMENT '用户名',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  nickname VARCHAR(80) NOT NULL COMMENT '昵称',
  role VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT '角色：USER、ADMIN',
  avatar_url VARCHAR(500) NULL COMMENT '头像',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  last_login_at DATETIME NULL COMMENT '最后登录时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dc_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS dc_user_favorite (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_product (user_id, product_id),
  KEY idx_favorite_user (user_id),
  KEY idx_favorite_product (product_id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES dc_product (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

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

CREATE TABLE IF NOT EXISTS dc_user_preference (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  min_budget DECIMAL(10,2) NULL,
  max_budget DECIMAL(10,2) NULL,
  category_id BIGINT UNSIGNED NULL,
  brand_ids VARCHAR(255) NULL,
  usage_scenes VARCHAR(500) NULL,
  priority_tags VARCHAR(500) NULL,
  avoid_tags VARCHAR(500) NULL,
  remark VARCHAR(1000) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_preference_user (user_id),
  CONSTRAINT fk_pref_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户选购偏好表';

CREATE TABLE IF NOT EXISTS dc_recommendation_log (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '推荐记录ID',
  user_id BIGINT UNSIGNED NULL COMMENT '用户ID',
  requirement TEXT NOT NULL COMMENT '用户需求',
  category_code VARCHAR(50) NULL COMMENT '目标分类',
  budget_min DECIMAL(10,2) NULL COMMENT '最低预算',
  budget_max DECIMAL(10,2) NULL COMMENT '最高预算',
  result_summary TEXT NULL COMMENT '推荐结果摘要',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dc_log_user (user_id),
  KEY idx_dc_log_category (category_code),
  CONSTRAINT fk_dc_log_user FOREIGN KEY (user_id) REFERENCES dc_user (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI推荐记录表';

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
  explain_summary VARCHAR(240) NULL COMMENT '推荐解释摘要',
  matched_requirements JSON NULL COMMENT '匹配到的用户需求',
  tradeoff_notes JSON NULL COMMENT '推荐权衡与风险点',
  knowledge_evidence JSON NULL COMMENT '命中的知识依据',
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
