USE digital_compass;

SET @rog_g14_main = 'https://digicompass-product-assets.oss-cn-beijing.aliyuncs.com/products/rog/rog-g14/main/rog-g14-main.png';

UPDATE dc_product
SET cover_url = @rog_g14_main
WHERE name = 'ROG 幻 14';

UPDATE dc_product_image
SET image_url = @rog_g14_main
WHERE product_id = (SELECT id FROM dc_product WHERE name = 'ROG 幻 14')
  AND image_type = 'MAIN'
  AND sort_order = 0;

SET @xiaomi_pad_6s_pro_main = 'https://digicompass-product-assets.oss-cn-beijing.aliyuncs.com/products/xiaomi/xiaomi-pad-6s-pro/main/xiaomi-pad-6s-pro-main.jpg';

UPDATE dc_product
SET cover_url = @xiaomi_pad_6s_pro_main
WHERE name = '小米平板 6S Pro';

UPDATE dc_product_image
SET image_url = @xiaomi_pad_6s_pro_main
WHERE product_id = (SELECT id FROM dc_product WHERE name = '小米平板 6S Pro')
  AND image_type = 'MAIN'
  AND sort_order = 0;

INSERT INTO dc_product_image (product_id, image_url, image_type, sort_order)
SELECT p.id, @xiaomi_pad_6s_pro_main, 'MAIN', 0
FROM dc_product p
WHERE p.name = '小米平板 6S Pro'
  AND NOT EXISTS (
      SELECT 1
      FROM dc_product_image i
      WHERE i.product_id = p.id
        AND i.image_type = 'MAIN'
        AND i.sort_order = 0
  );
