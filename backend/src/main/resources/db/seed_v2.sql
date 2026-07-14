USE digital_compass;

-- 补充更多产品
INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, 'iPhone 15 Pro', 'iPhone 15 Pro', '钛金属设计，A17 Pro 芯片，影像系统升级。', 7999.00, '2023-09-22', 90, 1
FROM dc_category c, dc_brand b WHERE c.code = 'phone' AND b.name = 'Apple'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, '小米 14 Pro', 'Xiaomi 14 Pro', '大屏旗舰，徕卡影像，性能强劲。', 4999.00, '2023-10-26', 87, 1
FROM dc_category c, dc_brand b WHERE c.code = 'phone' AND b.name = 'Xiaomi'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, '华为 Mate 60 Pro', 'HUAWEI Mate 60 Pro', '麒麟芯片回归，卫星通信，商务旗舰。', 6999.00, '2023-09-25', 89, 1
FROM dc_category c, dc_brand b WHERE c.code = 'phone' AND b.name = 'Huawei'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, '荣耀 Magic6 Pro', 'Honor Magic6 Pro', '长焦影像突出，护眼屏，续航强。', 5699.00, '2024-01-11', 85, 1
FROM dc_category c, dc_brand b WHERE c.code = 'phone' AND b.name = 'Honor'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, 'MacBook Pro 14', 'MacBook Pro 14 M3', '专业级性能，Mini-LED 屏幕，适合创作者。', 12999.00, '2023-11-07', 91, 1
FROM dc_category c, dc_brand b WHERE c.code = 'laptop' AND b.name = 'Apple'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, 'ThinkBook 14+', 'ThinkBook 14+ 2024', '接口丰富，性能释放强，学习办公轻创作。', 5499.00, '2024-03-01', 83, 1
FROM dc_category c, dc_brand b WHERE c.code = 'laptop' AND b.name = 'Lenovo'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

INSERT INTO dc_product (category_id, brand_id, brand, name, model, summary, official_price, release_date, score, status)
SELECT c.id, b.id, b.name, '华为 MateBook X Pro', 'HUAWEI MateBook X Pro 2024', '超轻薄，OLED 屏幕，商务旗舰本。', 11999.00, '2024-04-11', 86, 1
FROM dc_category c, dc_brand b WHERE c.code = 'laptop' AND b.name = 'Huawei'
ON DUPLICATE KEY UPDATE summary = VALUES(summary), official_price = VALUES(official_price), score = VALUES(score);

-- 为小米 14 补充更多参数
INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '影像', '主摄', '5000 万像素 徕卡', 30 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '续航', '快充', '90W 有线', 40 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '存储', '规格', '256GB / 512GB', 50 FROM dc_product p WHERE p.name = '小米 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

-- 为 iPhone 15 补充更多参数
INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '续航', '电池', '3349mAh', 30 FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '存储', '规格', '128GB / 256GB', 40 FROM dc_product p WHERE p.name = 'iPhone 15'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

-- 为 MacBook Air M2 补充更多参数
INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '13.6 英寸 Liquid Retina', 20 FROM dc_product p WHERE p.name = 'MacBook Air M2'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '续航', '时长', '最长 18 小时', 30 FROM dc_product p WHERE p.name = 'MacBook Air M2'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

-- 为新产品添加参数
INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'A17 Pro', 10 FROM dc_product p WHERE p.name = 'iPhone 15 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '6.1 英寸 ProMotion', 20 FROM dc_product p WHERE p.name = 'iPhone 15 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '材质', '机身', '钛金属', 30 FROM dc_product p WHERE p.name = 'iPhone 15 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', '第三代骁龙 8', 10 FROM dc_product p WHERE p.name = '小米 14 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '6.73 英寸 2K', 20 FROM dc_product p WHERE p.name = '小米 14 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '影像', '主拍', '5000 万像素 徕卡 Summilux', 30 FROM dc_product p WHERE p.name = '小米 14 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', '麒麟 9000S', 10 FROM dc_product p WHERE p.name = '华为 Mate 60 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '6.82 英寸', 20 FROM dc_product p WHERE p.name = '华为 Mate 60 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '特色', '卫星', '卫星通话', 30 FROM dc_product p WHERE p.name = '华为 Mate 60 Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'Apple M3 Pro', 10 FROM dc_product p WHERE p.name = 'MacBook Pro 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '14.2 英寸 Mini-LED', 20 FROM dc_product p WHERE p.name = 'MacBook Pro 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '内存', '规格', '18GB / 36GB', 30 FROM dc_product p WHERE p.name = 'MacBook Pro 14'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'Core Ultra 7 / Ryzen 7', 10 FROM dc_product p WHERE p.name = 'ThinkBook 14+'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '分辨率', '2.8K 120Hz', 20 FROM dc_product p WHERE p.name = 'ThinkBook 14+'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '接口', '扩展', 'HDMI / USB-A / USB-C / SD', 30 FROM dc_product p WHERE p.name = 'ThinkBook 14+'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '性能', '处理器', 'Core Ultra 9', 10 FROM dc_product p WHERE p.name = '华为 MateBook X Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '屏幕', '尺寸', '14.2 英寸 OLED', 20 FROM dc_product p WHERE p.name = '华为 MateBook X Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

INSERT INTO dc_product_spec (product_id, spec_group, spec_name, spec_value, sort_order)
SELECT p.id, '重量', '机身', '约 0.98kg', 30 FROM dc_product p WHERE p.name = '华为 MateBook X Pro'
ON DUPLICATE KEY UPDATE spec_value = VALUES(spec_value);

-- 为新产品添加价格参考
INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 6200.00, 7200.00, 6700.00, 15, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = 'iPhone 15 Pro'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 3500.00, 4200.00, 3850.00, 15, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = '小米 14 Pro'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 5200.00, 6200.00, 5700.00, 15, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = '华为 Mate 60 Pro'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 4200.00, 5000.00, 4600.00, 12, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = '荣耀 Magic6 Pro'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 9800.00, 11500.00, 10650.00, 10, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = 'MacBook Pro 14'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 3600.00, 4600.00, 4100.00, 12, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = 'ThinkBook 14+'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

INSERT INTO dc_price_reference (product_id, price_type, platform, min_price, max_price, avg_price, sample_count, reference_date, source_type, remark)
SELECT p.id, 'used', '二手行情估算', 8500.00, 10000.00, 9250.00, 8, CURRENT_DATE, 'manual', '演示数据'
FROM dc_product p WHERE p.name = '华为 MateBook X Pro'
ON DUPLICATE KEY UPDATE min_price = VALUES(min_price), max_price = VALUES(max_price), avg_price = VALUES(avg_price);

-- 为新产品添加购买链接
INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.apple.com.cn/', 10, 1 FROM dc_product p WHERE p.name = 'iPhone 15 Pro'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.mi.com/', 10, 1 FROM dc_product p WHERE p.name = '小米 14 Pro'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.vmall.com/', 10, 1 FROM dc_product p WHERE p.name = '华为 Mate 60 Pro'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.honor.com/', 10, 1 FROM dc_product p WHERE p.name = '荣耀 Magic6 Pro'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.apple.com.cn/macbook-pro/', 10, 1 FROM dc_product p WHERE p.name = 'MacBook Pro 14'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '京东', 'store', CONCAT(p.name, ' 京东'), 'https://www.jd.com/', 10, 1 FROM dc_product p WHERE p.name = 'ThinkBook 14+'
ON DUPLICATE KEY UPDATE url = VALUES(url);

INSERT INTO dc_purchase_link (product_id, platform, link_type, title, url, sort_order, enabled)
SELECT p.id, '官网', 'official', CONCAT(p.name, ' 官网'), 'https://www.vmall.com/', 10, 1 FROM dc_product p WHERE p.name = '华为 MateBook X Pro'
ON DUPLICATE KEY UPDATE url = VALUES(url);
