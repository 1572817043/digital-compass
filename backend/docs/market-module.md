# DigiCompass 价格行情模块记录

记录日期：2026-07-11

## 1. 模块目标

价格行情模块用于展示产品库中的价格参考数据。用户可以在 `/market` 页面查看官方价、二手参考价和渠道参考价，并从行情卡片跳转到产品详情页。

本模块为用户端公开浏览功能，不要求登录。后端接口不读取用户身份，也不接收 `userId`。

## 2. 数据来源

行情数据来自已有价格参考表：

| 表名 | 说明 |
| --- | --- |
| `dc_price_reference` | 保存产品不同价格类型、平台来源、价格区间、均价、样本数和参考日期 |

行情列表只展示每个产品、每种价格类型的最新一条记录。最新记录按 `reference_date DESC, id DESC` 判断。

## 3. 后端接口

后端项目路径：

```text
/Users/a0000/IdeaProjects/digital-compass-backend
```

本模块接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/market/prices` | 查询行情列表，支持关键词、分类、品牌、价格类型和排序 |
| `GET` | `/api/market/products/{productId}/prices` | 查询某个产品的全部价格参考记录 |
| `GET` | `/api/market/summary` | 查询行情页统计数据 |

`GET /api/market/prices` 支持参数：

| 参数 | 说明 |
| --- | --- |
| `keyword` | 产品名或品牌关键词 |
| `categoryId` | 分类 ID |
| `brandId` | 品牌 ID |
| `priceType` | 价格类型，支持 `official`、`used`、`channel` |
| `sort` | 排序方式，支持 `latest`、`priceAsc`、`priceDesc`、`scoreDesc` |

## 4. 后端文件

| 文件 | 职责 |
| --- | --- |
| `modules/market/controller/MarketController.java` | 价格行情 REST 接口 |
| `modules/market/service/MarketService.java` | 行情服务接口 |
| `modules/market/service/impl/MarketServiceImpl.java` | 行情服务实现 |
| `modules/market/mapper/MarketMapper.java` | 行情 SQL 查询 |
| `modules/market/dto/MarketPriceItem.java` | 行情列表项 |
| `modules/market/dto/MarketProductPriceRecord.java` | 产品价格参考记录 |
| `modules/market/dto/MarketSummary.java` | 行情统计数据 |

## 5. 前端文件

前端项目路径：

```text
/Users/a0000/Projects/digital-compass-frontend
```

本模块涉及文件：

| 文件 | 职责 |
| --- | --- |
| `src/api/market.ts` | 封装行情接口 |
| `src/types/market.ts` | 定义行情数据类型 |
| `src/views/user/MarketView.vue` | 价格行情页面 |
| `src/router/index.ts` | `/market` 路由 |
| `src/components/common/AppHeader.vue` | 用户端导航入口 |

## 6. 页面功能

`/market` 页面包含：

- 顶部标题和说明。
- 统计卡片：产品总数、有行情产品、二手参考、最近更新。
- 筛选区：关键词、分类、品牌、价格类型、排序。
- 行情卡片：产品图、产品名称、品牌、分类、推荐分、价格类型、均价、价格区间、官方价、样本数、更新时间。
- 操作入口：查看产品、设置提醒。
- 空状态：无符合条件的数据时显示提示。

## 7. 验证记录

已执行后端测试：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw test
```

结果：

```text
Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

已执行前端构建：

```bash
cd /Users/a0000/Projects/digital-compass-frontend
npm run build
```

结果：

```text
vue-tsc --noEmit && vite build
✓ built
```

已执行真实接口验证：

| 接口 | 结果 |
| --- | --- |
| `GET /api/market/prices?priceType=used&sort=latest` | 200 |
| `GET /api/market/products/19/prices` | 200 |
| `GET /api/market/summary` | 200 |

已执行浏览器验证：

| 验证项 | 结果 |
| --- | --- |
| `/market` 页面显示标题和统计卡片 | 通过 |
| 行情列表显示 30 条卡片 | 通过 |
| 搜索 `iPhone 16 Pro` | 通过 |
| 切换二手参考筛选 | 通过 |
| 点击“查看产品”跳转详情页 | 通过 |
| 控制台错误 | 0 |
| 4xx/5xx 响应 | 0 |
