USE digital_compass;

INSERT INTO dc_category (code, name, description, sort_order, enabled)
VALUES
  ('phone', '手机', '智能手机产品库', 10, 1),
  ('laptop', '笔记本电脑', '轻薄本、全能本等电脑产品库', 20, 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

INSERT INTO dc_product (category_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, 'Xiaomi', '小米 14', 'Xiaomi 14', '小屏旗舰，性能和影像比较均衡，二手价格友好。', 4299.00, '2023-10-26', 88, 1
FROM dc_category c WHERE c.code = 'phone'
ON DUPLICATE KEY UPDATE
  summary = VALUES(summary),
  official_price = VALUES(official_price),
  release_date = VALUES(release_date),
  score = VALUES(score),
  status = VALUES(status);

INSERT INTO dc_product (category_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, 'Apple', 'iPhone 15', 'iPhone 15', '系统生态稳定，视频体验好，适合长期使用。', 5999.00, '2023-09-13', 86, 1
FROM dc_category c WHERE c.code = 'phone'
ON DUPLICATE KEY UPDATE
  summary = VALUES(summary),
  official_price = VALUES(official_price),
  release_date = VALUES(release_date),
  score = VALUES(score),
  status = VALUES(status);

INSERT INTO dc_product (category_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, 'Apple', 'MacBook Air M2', 'MacBook Air M2', '轻薄、安静、续航稳定，适合学生和轻办公。', 7999.00, '2022-06-07', 82, 1
FROM dc_category c WHERE c.code = 'laptop'
ON DUPLICATE KEY UPDATE
  summary = VALUES(summary),
  official_price = VALUES(official_price),
  release_date = VALUES(release_date),
  score = VALUES(score),
  status = VALUES(status);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', '第三代骁龙 8', 10 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '6.36 英寸', 20 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'A16 Bionic', 10 FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '6.1 英寸', 20 FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'Apple M2', 10 FROM dc_product p WHERE p.name = 'MacBook Air M2'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value), sort_order = VALUES(sort_order);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 2800.00, 3400.00, 3100.00, 20, CURRENT_DATE, 'manual', '演示数据，后续可由后台维护'
FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE
  min_price = VALUES(min_price),
  max_price = VALUES(max_price),
  avg_price = VALUES(avg_price),
  sample_count = VALUES(sample_count),
  remark = VALUES(remark);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 3900.00, 4600.00, 4250.00, 20, CURRENT_DATE, 'manual', '演示数据，后续可由后台维护'
FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE
  min_price = VALUES(min_price),
  max_price = VALUES(max_price),
  avg_price = VALUES(avg_price),
  sample_count = VALUES(sample_count),
  remark = VALUES(remark);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 4800.00, 5900.00, 5350.00, 16, CURRENT_DATE, 'manual', '演示数据，后续可由后台维护'
FROM dc_product p WHERE p.name = 'MacBook Air M2'
ON DUPLICATE KEY UPDATE
  min_price = VALUES(min_price),
  max_price = VALUES(max_price),
  avg_price = VALUES(avg_price),
  sample_count = VALUES(sample_count),
  remark = VALUES(remark);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.mi.com/', 10, 1 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE
  url = VALUES(url),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.apple.com.cn/', 10, 1 FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE
  url = VALUES(url),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.apple.com.cn/macbook-air/', 10, 1 FROM dc_product p WHERE p.name = 'MacBook Air M2'
ON DUPLICATE KEY UPDATE
  url = VALUES(url),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled);

INSERT INTO dc_ai_knowledge (category_id, product_id, title, content, knowledge_type, tags, source, status)
SELECT c.id, NULL, '二手手机验机重点', '二手手机重点检查电池健康、屏幕显示、维修记录、序列号和保修状态。', 'risk', '二手,验机,手机', 'manual', 1
FROM dc_category c WHERE c.code = 'phone'
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  knowledge_type = VALUES(knowledge_type),
  tags = VALUES(tags),
  status = VALUES(status);

INSERT INTO dc_ai_knowledge (category_id, product_id, title, content, knowledge_type, tags, source, status)
SELECT c.id, NULL, '学生轻薄本选择思路', '学生轻办公优先关注重量、续航、屏幕素质、内存容量和售后保修。', 'guide', '笔记本,学生,办公', 'manual', 1
FROM dc_category c WHERE c.code = 'laptop'
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  knowledge_type = VALUES(knowledge_type),
  tags = VALUES(tags),
  status = VALUES(status);

INSERT INTO dc_user (username, password_hash, nickname, role, status)
VALUES ('admin', '$2a$10$hJr1eYDgPL5afm39EiBpLesPh9PKvdcVd.ODtV7leF88eWHtrpWBS', '管理员', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status);
