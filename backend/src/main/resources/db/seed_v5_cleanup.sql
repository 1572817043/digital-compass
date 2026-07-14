USE digital_compass;

UPDATE dc_product
SET status = 0
WHERE brand = 'Apple'
  AND name = 'MacBook Pro 14'
  AND model = 'MacBook Pro 14 M3'
  AND cover_url IS NULL;

UPDATE dc_product
SET status = 0
WHERE brand = 'Lenovo'
  AND name = 'ThinkBook 14+'
  AND model = 'ThinkBook 14+ 2024'
  AND cover_url IS NULL;

UPDATE dc_product
SET status = 0
WHERE brand = 'Huawei'
  AND name = '华为 MateBook X Pro'
  AND model = 'HUAWEI MateBook X Pro 2024'
  AND cover_url IS NULL;
