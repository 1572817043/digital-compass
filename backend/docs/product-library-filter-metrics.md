# DigiCompass 产品库标准指标与筛选增强记录

记录日期：2026-07-11

## 1. 模块目标

本轮优化在已有产品库基础上补充两类能力：

- 标准化核心指标：把动态参数中的处理器、屏幕、电池、重量等信息整理成固定字段结构，便于后续产品对比和 AI 推荐使用。
- 产品列表筛选增强：用户端产品库支持价格区间、用途标签、二手参考、购买链接和排序筛选。

该模块不替代原有动态参数。原有 `dc_product_spec` 继续负责展示完整参数，新增 `dc_product_metric` 负责对比、筛选和推荐计算。

## 2. 数据库设计

新增表：`dc_product_metric`

表文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/migration/V4__product_metrics.sql`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/schema.sql`

核心字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 核心指标 ID |
| `product_id` | 产品 ID，关联 `dc_product.id` |
| `metric_key` | 指标编码，例如 `processor`、`battery_capacity` |
| `metric_label` | 页面展示名称 |
| `metric_value` | 页面展示值 |
| `numeric_value` | 可计算数值 |
| `unit` | 单位 |
| `sort_order` | 排序值 |

外键规则：

```sql
CONSTRAINT fk_dc_metric_product
FOREIGN KEY (product_id) REFERENCES dc_product (id)
ON DELETE CASCADE
```

产品删除后，相关标准指标会自动删除。

## 3. 标准指标种子数据

新增种子文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/seed_v4.sql`

该文件从已有 `dc_product_spec` 中抽取常见参数，写入 `dc_product_metric`。

当前抽取的指标：

| `metric_key` | 页面名称 | 来源 |
| --- | --- | --- |
| `processor` | 处理器 | `dc_product_spec.spec_name = '处理器'` |
| `screen_size` | 屏幕尺寸 | `spec_group = '屏幕'` 且 `spec_name = '尺寸'` |
| `refresh_rate` | 刷新率 | `spec_name = '刷新率'` |
| `battery_capacity` | 电池容量 | `spec_name = '电池'` |
| `charging_power` | 快充功率 | `spec_name = '快充'` |
| `storage` | 存储规格 | `spec_group IN ('存储', '内存')` |
| `weight` | 重量 | `spec_name = '重量'` |
| `os` | 操作系统 | `spec_name = '操作系统'` |

本机数据库已导入 `seed_v4.sql`，当前 `dc_product_metric` 中有 185 条数据。

## 4. 后端实现

### 4.1 DTO

新增文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/dto/ProductMetricItem.java`

结构：

```java
public record ProductMetricItem(
        Long id,
        String metricKey,
        String metricLabel,
        String metricValue,
        BigDecimal numericValue,
        String unit,
        int sortOrder
) {
}
```

### 4.2 产品详情接口

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/dto/ProductDetail.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/mapper/ProductMapper.java`

`GET /api/products/{id}` 新增返回字段：

```json
{
  "metrics": [
    {
      "id": 1,
      "metricKey": "processor",
      "metricLabel": "处理器",
      "metricValue": "A18 Pro",
      "numericValue": null,
      "unit": null,
      "sortOrder": 10
    }
  ]
}
```

### 4.3 产品列表筛选接口

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/controller/ProductController.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/service/ProductService.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/service/impl/ProductServiceImpl.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/mapper/ProductMapper.java`

`GET /api/products` 新增查询参数：

| 参数 | 说明 |
| --- | --- |
| `minPrice` | 官方价最低值 |
| `maxPrice` | 官方价最高值 |
| `tagType` | 标签类型 |
| `tagName` | 标签名称 |
| `hasUsedPrice` | 是否要求存在二手参考价 |
| `hasPurchaseLink` | 是否要求存在购买链接 |
| `sortBy` | 排序方式 |

`sortBy` 当前支持：

| 值 | 说明 |
| --- | --- |
| 空 | 推荐分从高到低 |
| `price_asc` | 官方价从低到高 |
| `price_desc` | 官方价从高到低 |
| `score_asc` | 推荐分从低到高 |
| `newest` | 发售时间从新到旧 |

筛选逻辑在 SQL 层完成，不是前端本地过滤。

## 5. 前端实现

前端项目路径：

- `/Users/a0000/Projects/digital-compass-frontend`

### 5.1 类型与 API

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/types/product.ts`
- `/Users/a0000/Projects/digital-compass-frontend/src/api/product.ts`

新增类型：

- `ProductMetricItem`

`ProductDetail` 新增：

```ts
metrics: ProductMetricItem[];
```

`fetchProducts` 新增筛选参数：

```ts
minPrice?: number;
maxPrice?: number;
tagType?: string;
tagName?: string;
hasUsedPrice?: boolean;
hasPurchaseLink?: boolean;
sortBy?: string;
```

### 5.2 产品 Store

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/stores/productStore.ts`

新增状态：

- `minPrice`
- `maxPrice`
- `tagName`
- `sortBy`
- `hasUsedPrice`
- `hasPurchaseLink`

新增方法：

- `resetFilters()`

### 5.3 产品列表页

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/views/user/ProductListView.vue`

新增筛选项：

- 最低价
- 最高价
- 用途标签
- 排序方式
- 有二手参考
- 有购买链接
- 重置筛选

筛选项变化后会重新请求 `GET /api/products`。

### 5.4 产品详情页

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/views/user/ProductDetailView.vue`

新增“关键指标”区域。

页面顺序：

1. 图集
2. 关键指标
3. 核心参数
4. 选购判断
5. 购买链接

“关键指标”来自 `ProductDetail.metrics`，用于快速扫读和后续产品对比。

### 5.5 图片失败兜底

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/components/product/ProductCard.vue`
- `/Users/a0000/Projects/digital-compass-frontend/src/views/user/ProductDetailView.vue`

产品卡片和详情主图增加图片加载失败处理。图片 URL 返回 404 时，页面会显示原有分类占位图，避免出现浏览器破图标。

### 5.6 图片完整展示

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/components/product/ProductCard.vue`
- `/Users/a0000/Projects/digital-compass-frontend/src/views/user/ProductDetailView.vue`

产品卡片和详情页主图使用 `object-fit: contain`。图片会完整显示在固定图片区域内，避免手机、平板、电脑主图被裁切。卡片图片区域和详情页图片区域增加内边距，用于保留产品主体周围留白。

### 5.7 旧演示数据下架

新增种子文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/seed_v5_cleanup.sql`

该文件将早期演示用的三条无图笔记本产品置为下架状态：

| 旧产品 | 保留展示的完整产品 |
| --- | --- |
| `MacBook Pro 14` | `MacBook Pro 14 M3` |
| `ThinkBook 14+` | `ThinkBook 14+ 2024` |
| `华为 MateBook X Pro` | `华为 MateBook X Pro 2024` |

处理方式为更新 `dc_product.status = 0`。产品记录不会被删除，用户端产品列表只返回 `status = 1` 的产品。

### 5.8 OSS 图片修正

新增种子文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/seed_v6_image_cleanup.sql`

该文件修正两条失效图片数据：

| 产品 | 处理 |
| --- | --- |
| `ROG 幻 14` | 原 OSS 对象内容为 HTML 错误页，重新上传真实 PNG 图片并更新 `cover_url` 与主图记录 |
| `小米平板 6S Pro` | 原小米 CDN 链接返回 404，重新上传真实 JPEG 图片并补齐主图记录 |

后端上传服务同步补充 OSS 对象元数据：

- `Content-Type` 使用上传文件的真实 MIME 类型。
- `Content-Length` 使用上传文件大小。
- `Content-Disposition` 写入 `inline` 文件名。

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/upload/UploadServiceImpl.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/test/java/com/a0000/digicompass/modules/upload/UploadServiceImplTests.java`

## 6. 测试记录

### 6.1 后端自动化测试

新增测试文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/test/java/com/a0000/digicompass/ProductCatalogOptimizationTests.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/test/java/com/a0000/digicompass/modules/upload/UploadServiceImplTests.java`

覆盖内容：

- 产品列表支持价格区间和标签名称联合筛选。
- 产品列表支持官方价从低到高排序。
- 产品详情接口返回 `metrics` 数组。
- OSS 图片上传时写入浏览器可识别的图片元数据。

已执行命令：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw test
```

执行结果：

```text
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 6.2 前端构建验证

已执行命令：

```bash
cd /Users/a0000/Projects/digital-compass-frontend
npm run build
```

执行结果：

```text
vue-tsc --noEmit && vite build
✓ built
```

### 6.3 页面联调验证

已验证页面：

- `http://localhost:5175/products`
- `http://localhost:5175/products/35`
- `http://localhost:5175/products/42`

验证结果：

| 验证项 | 结果 |
| --- | --- |
| 产品列表返回数量 | 30 |
| 旧演示笔记本是否仍在用户端列表 | 否 |
| `ROG 幻 14` 图片是否加载 | 是，1000 × 620 |
| `小米平板 6S Pro` 图片是否加载 | 是，1200 × 1200 |
| 卡片主图展示方式 | `object-fit: contain` |
| 列表页控制台错误 | 0 |
| 列表页失败请求 | 0 |
| 列表页 4xx/5xx 响应 | 0 |

## 7. 后续建议

产品库当前已经具备产品对比模块所需的核心数据：

- 产品主信息
- 图片
- 动态参数
- 标准指标
- 价格参考
- 购买链接
- 标签、优点、短板、适合人群和使用场景

下一步建议进入“产品对比模块”。

对比模块可以直接复用：

- `GET /api/products`
- `GET /api/products/{id}`
- `ProductDetail.metrics`
- `ProductDetail.specs`
- `ProductDetail.tags`
- `ProductDetail.prices`

建议对比模块第一版只支持 2 到 3 个产品，不做复杂登录态保存，先使用前端本地状态保存待对比产品。
