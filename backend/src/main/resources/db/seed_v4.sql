USE digital_compass;

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'processor', '处理器', spec_value, NULL, NULL, 10
FROM dc_product_spec
WHERE spec_name = '处理器'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'screen_size', '屏幕尺寸', spec_value, CAST(REGEXP_SUBSTR(spec_value, '[0-9]+(\\.[0-9]+)?') AS DECIMAL(12,2)), '英寸', 20
FROM dc_product_spec
WHERE spec_group = '屏幕' AND spec_name = '尺寸'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'refresh_rate', '刷新率', spec_value, CAST(REGEXP_SUBSTR(spec_value, '[0-9]+(\\.[0-9]+)?') AS DECIMAL(12,2)), 'Hz', 30
FROM dc_product_spec
WHERE spec_name = '刷新率'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'battery_capacity', '电池容量', spec_value, CAST(REGEXP_SUBSTR(spec_value, '[0-9]+(\\.[0-9]+)?') AS DECIMAL(12,2)), 'mAh', 40
FROM dc_product_spec
WHERE spec_name = '电池'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'charging_power', '快充功率', spec_value, CAST(REGEXP_SUBSTR(spec_value, '[0-9]+(\\.[0-9]+)?') AS DECIMAL(12,2)), 'W', 50
FROM dc_product_spec
WHERE spec_name = '快充'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'storage', '存储规格', spec_value, NULL, NULL, 60
FROM dc_product_spec
WHERE spec_name IN ('容量', '规格') AND spec_group IN ('存储', '内存')
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'weight', '重量', spec_value, CAST(REGEXP_SUBSTR(spec_value, '[0-9]+(\\.[0-9]+)?') AS DECIMAL(12,2)), CASE WHEN spec_value LIKE '%kg%' THEN 'kg' ELSE 'g' END, 70
FROM dc_product_spec
WHERE spec_name = '重量'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);

INSERT INTO dc_product_metric (product_id, metric_key, metric_label, metric_value, numeric_value, unit, sort_order)
SELECT product_id, 'os', '操作系统', spec_value, NULL, NULL, 80
FROM dc_product_spec
WHERE spec_name = '操作系统'
ON DUPLICATE KEY UPDATE metric_label = VALUES(metric_label), metric_value = VALUES(metric_value), numeric_value = VALUES(numeric_value), unit = VALUES(unit), sort_order = VALUES(sort_order);
