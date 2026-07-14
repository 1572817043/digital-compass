USE digital_compass;

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'selling_point', '影像强', '适合重视拍照、视频和长焦体验的用户', 10 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPhone 15 Pro', '小米 14 Ultra', '华为 Mate 60 Pro', 'OPPO Find X7 Ultra', 'vivo X100 Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'selling_point', '性能强', '适合游戏、多任务和高负载应用', 20 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPhone 15 Pro', '小米 14 Ultra', 'MacBook Pro 14 M3', '联想拯救者 Y7000P 2024', 'ROG 幻 14')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'selling_point', '屏幕好', '适合看视频、修图和长时间阅读', 30 FROM dc_product p WHERE p.name IN ('iPad Pro M4', 'iPad Air M2', '华为 MatePad Pro', 'MacBook Pro 14 M3', '华为 MateBook X Pro 2024', '小米平板 6S Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'selling_point', '轻薄便携', '适合通勤、上课和移动办公', 40 FROM dc_product p WHERE p.name IN ('MacBook Air M3', 'MacBook Air M2', '华为 MateBook X Pro 2024', 'ThinkBook 14+ 2024', 'iPad Air M2')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'selling_point', '生态体验好', '适合已有同品牌设备的用户', 50 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPhone 16', 'iPhone 15 Pro', 'iPad Pro M4', 'MacBook Air M3', 'AirPods Pro 2', 'Apple Watch Series 9')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'weakness', '价格高', '预算敏感用户需要重点比较同价位替代机型', 10 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPhone 15 Pro', 'iPad Pro M4', 'MacBook Pro 14 M3', '华为 MateBook X Pro 2024', '小米 14 Ultra')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'weakness', '机身偏重', '长时间手持或通勤携带前建议关注重量', 20 FROM dc_product p WHERE p.name IN ('小米 14 Ultra', '华为 Mate 60 Pro', 'OPPO Find X7 Ultra', '联想拯救者 Y7000P 2024', 'ROG 幻 14')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'suitable', '学生党', '适合课程学习、资料整理和日常娱乐', 10 FROM dc_product p WHERE p.name IN ('iPhone 16', 'iPhone 15', 'MacBook Air M2', 'MacBook Air M3', 'ThinkBook 14+ 2024', '小米平板 6S Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'suitable', '创作者', '适合图片、视频、文档和多屏协作场景', 20 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPad Pro M4', 'MacBook Pro 14 M3', '华为 MateBook X Pro 2024', '华为 MatePad Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'suitable', '通勤办公', '适合轻办公、会议、邮件和文档处理', 30 FROM dc_product p WHERE p.name IN ('MacBook Air M3', 'MacBook Air M2', 'ThinkBook 14+ 2024', '华为 MateBook X Pro 2024', 'Apple Watch Series 9', '华为 Watch GT 4')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'unsuitable', '极致性价比用户', '同预算下可能存在价格更低的替代选择', 10 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPad Pro M4', 'MacBook Pro 14 M3', 'AirPods Pro 2')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'scene', '拍照旅行', '适合记录旅行、人像、夜景和视频素材', 10 FROM dc_product p WHERE p.name IN ('iPhone 16 Pro', 'iPhone 15 Pro', '小米 14 Ultra', '华为 Mate 60 Pro', 'OPPO Find X7 Ultra', 'vivo X100 Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'scene', '学习办公', '适合文档、网课、会议和资料管理', 20 FROM dc_product p WHERE p.name IN ('MacBook Air M3', 'MacBook Air M2', 'ThinkBook 14+ 2024', 'iPad Air M2', '小米平板 6S Pro')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);

INSERT INTO dc_product_tag (product_id, tag_type, tag_name, tag_value, sort_order)
SELECT p.id, 'scene', '运动健康', '适合日常运动记录、睡眠监测和通知提醒', 30 FROM dc_product p WHERE p.name IN ('Apple Watch Series 9', '华为 Watch GT 4', '小米 Watch S3')
ON DUPLICATE KEY UPDATE tag_value = VALUES(tag_value), sort_order = VALUES(sort_order);
