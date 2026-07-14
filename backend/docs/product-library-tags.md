# DigiCompass 产品库标签与选购判断模块记录

记录日期：2026-07-11

## 1. 模块目标

产品库原有能力包括产品主信息、图片、参数、价格参考和购买链接。当前新增“产品标签与选购判断”能力，用于把产品从“参数展示”升级为“选购决策数据”。

该模块主要服务三个后续方向：

- 产品详情页展示核心优点、主要短板、适合人群、不适合人群和使用场景。
- 产品对比模块可以直接复用标签、参数、价格和购买链接。
- AI 选购助手可以基于结构化标签生成推荐理由和风险提醒。

## 2. 数据库设计

新增表：`dc_product_tag`

表文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/migration/V3__product_tags.sql`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/schema.sql`

核心字段：

| 字段 | 说明 |
| --- | --- |
| `id` | 标签 ID |
| `product_id` | 产品 ID，关联 `dc_product.id` |
| `tag_type` | 标签类型 |
| `tag_name` | 标签名称 |
| `tag_value` | 标签说明 |
| `sort_order` | 排序值 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

`tag_type` 当前固定为 5 类：

| 值 | 页面含义 |
| --- | --- |
| `selling_point` | 核心优点 |
| `weakness` | 主要短板 |
| `suitable` | 适合人群 |
| `unsuitable` | 不适合人群 |
| `scene` | 使用场景 |

外键规则：

```sql
CONSTRAINT fk_dc_tag_product
FOREIGN KEY (product_id) REFERENCES dc_product (id)
ON DELETE CASCADE
```

产品删除后，相关标签会自动删除。

## 3. 种子数据

新增种子文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/seed_v3.sql`

当前本机数据库已经导入该文件，`dc_product_tag` 中有 76 条标签数据。

覆盖的产品类型包括：

- 手机：iPhone、小米、华为、OPPO、vivo
- 笔记本电脑：MacBook、ThinkBook、ROG、MateBook
- 平板：iPad、MatePad、小米平板
- 耳机：AirPods、FreeBuds、小米 Buds
- 手表：Apple Watch、华为 Watch、小米 Watch

示例数据：

| 产品 | 标签类型 | 标签名 | 说明 |
| --- | --- | --- | --- |
| iPhone 16 Pro | 核心优点 | 影像强 | 适合重视拍照、视频和长焦体验的用户 |
| iPhone 16 Pro | 主要短板 | 价格高 | 预算敏感用户需要重点比较同价位替代机型 |
| MacBook Air M3 | 适合人群 | 通勤办公 | 适合轻办公、会议、邮件和文档处理 |
| Apple Watch Series 9 | 使用场景 | 运动健康 | 适合日常运动记录、睡眠监测和通知提醒 |

## 4. 后端实现

### 4.1 DTO

新增文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/dto/ProductTagItem.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/dto/ProductTagSaveRequest.java`

`ProductTagItem` 用于接口返回：

```java
public record ProductTagItem(
        Long id,
        String tagType,
        String tagName,
        String tagValue,
        int sortOrder
) {
}
```

`ProductTagSaveRequest` 用于新增和编辑标签，包含校验规则：

- `tagType` 必填，并限制在 `selling_point|weakness|suitable|unsuitable|scene`
- `tagName` 必填，最长 80
- `tagValue` 最长 500
- `sortOrder` 可为空

### 4.2 产品详情返回结构

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/dto/ProductDetail.java`

`ProductDetail` 新增字段：

```java
List<ProductTagItem> tags
```

用户端接口 `GET /api/products/{id}` 返回产品详情时，会一起返回 `tags`。

### 4.3 Mapper

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/mapper/ProductMapper.java`

新增方法：

- `findTags(Long productId)`
- `insertTag(Long productId, String tagType, String tagName, String tagValue, int sortOrder)`
- `updateTag(Long tagId, Long productId, String tagType, String tagName, String tagValue, int sortOrder)`
- `deleteTag(Long tagId, Long productId)`

标签查询排序规则：

```sql
ORDER BY FIELD(tag_type, 'selling_point', 'weakness', 'suitable', 'unsuitable', 'scene'), sort_order, id
```

因此页面展示顺序固定为：核心优点、主要短板、适合人群、不适合人群、使用场景。

### 4.4 Service

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/service/ProductService.java`
- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/service/impl/ProductServiceImpl.java`

新增服务方法：

- `listTags(Long productId)`
- `createTag(Long productId, ProductTagSaveRequest request)`
- `updateTag(Long productId, Long tagId, ProductTagSaveRequest request)`
- `deleteTag(Long productId, Long tagId)`

### 4.5 Controller

修改文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/modules/product/controller/AdminProductMetaController.java`

新增管理端接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/products/{productId}/tags` | 查询产品标签 |
| `POST` | `/api/admin/products/{productId}/tags` | 新增产品标签 |
| `PUT` | `/api/admin/products/{productId}/tags/{tagId}` | 编辑产品标签 |
| `DELETE` | `/api/admin/products/{productId}/tags/{tagId}` | 删除产品标签 |

这些接口走已有 Spring Security 规则：`/api/admin/**` 需要 `ADMIN` 角色。

## 5. 前端实现

前端项目路径：

- `/Users/a0000/Projects/digital-compass-frontend`

### 5.1 类型定义

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/types/product.ts`

新增类型：

- `ProductTagItem`
- `ProductTagPayload`

`ProductDetail` 新增：

```ts
tags: ProductTagItem[];
```

同时保留 `Product` mock 类型，避免旧 mock 数据影响构建。

### 5.2 API 封装

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/api/product.ts`

新增方法：

- `fetchAdminProductTags(productId)`
- `createProductTag(productId, data)`
- `updateProductTag(productId, tagId, data)`
- `deleteProductTag(productId, tagId)`

### 5.3 用户端详情页

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/views/user/ProductDetailView.vue`

新增“选购判断”区域。

展示规则：

- 按 `tag_type` 分组显示。
- 没有标签的分组不显示。
- 没有任何标签时，整个“选购判断”区域不显示。

页面分组名称：

- 核心优点
- 主要短板
- 适合人群
- 不适合人群
- 使用场景

### 5.4 管理端产品维护

修改文件：

- `/Users/a0000/Projects/digital-compass-frontend/src/views/admin/AdminProductsView.vue`

资料维护弹窗新增“标签”Tab。

支持操作：

- 查询产品标签
- 新增标签
- 编辑标签
- 删除标签

`tag_type` 使用下拉框，不需要手动输入。

下拉项：

- 核心优点
- 主要短板
- 适合人群
- 不适合人群
- 使用场景

## 6. 测试记录

### 6.1 后端自动化测试

新增测试文件：

- `/Users/a0000/IdeaProjects/digital-compass-backend/src/test/java/com/a0000/digicompass/ProductTagTests.java`

覆盖内容：

- 产品详情接口返回 `tags`
- 管理员可以查询标签列表
- 管理员可以新增、编辑、删除标签
- 无 token 访问管理端标签接口返回 401
- 普通用户访问管理端标签接口返回 403

已执行命令：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw test
```

执行结果：

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
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

### 6.3 真实接口验证

验证接口：

```bash
curl http://localhost:8081/api/products/19
```

接口返回的 `tags` 示例：

```text
selling_point 影像强
selling_point 性能强
selling_point 生态体验好
weakness 价格高
suitable 创作者
unsuitable 极致性价比用户
scene 拍照旅行
```

### 6.4 页面验证

验证页面：

```text
http://localhost:5175/products/19
```

页面结果：

- 产品详情页右侧显示“选购判断”区域。
- `iPhone 16 Pro` 能显示核心优点、主要短板、适合人群、不适合人群和使用场景。
- 页面能正常渲染，产品详情接口返回正常。

## 7. 当前运行状态

当前本机运行端口：

| 服务 | 地址 |
| --- | --- |
| 后端 Spring Boot | `http://localhost:8081` |
| 前端 Vite | `http://localhost:5175` |

后端已经用本次代码重新启动。

## 8. 注意事项

测试类 `ProductTagTests` 会创建一个名为“标签测试产品”的测试数据。该测试产品的 `status` 为 `0`，不会出现在用户端产品列表中。

`seed_v3.sql` 使用 `ON DUPLICATE KEY UPDATE`，可以重复执行，不会重复插入同名标签。

本模块不包含 AI 调用逻辑。它提供结构化选购数据，后续 AI 选购助手可以读取这些标签生成推荐理由。

## 9. 后续建议

下一步建议开发“产品对比模块”。

原因：

- 产品参数、价格、购买链接和选购标签已经具备。
- 对比模块可以直接复用当前产品详情数据。
- 对比结果后续可以作为 AI 推荐解释的基础。

建议对比模块包含：

- 选择 2 到 3 个产品加入对比。
- 对比基础信息、价格、核心参数、优点、短板和适合人群。
- 支持从产品详情页和产品列表页加入对比。
- 保存最近一次对比结果到浏览器本地状态。
