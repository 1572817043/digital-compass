# DigiCompass 价格提醒模块记录

记录日期：2026-07-11

## 1. 模块目标

价格提醒模块用于让登录用户为某个产品设置目标价。系统读取产品价格参考表中的最新价格，当最新参考价小于或等于目标价时，提醒状态显示为“已达到目标价”。

价格提醒属于用户个人数据。后端通过 JSON Web Token（JWT）识别当前登录用户，接口不从前端接收 `userId`。

## 2. 数据表

本模块新增一张表：

| 表名 | 说明 |
| --- | --- |
| `dc_price_alert` | 用户价格提醒表，保存用户、产品、目标价、价格类型、最新参考价和提醒状态 |

建表脚本：

```text
/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/migration/V8__price_alert.sql
```

初始化脚本同步位置：

```text
/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/schema.sql
```

核心字段：

| 字段 | 说明 |
| --- | --- |
| `user_id` | 用户 ID |
| `product_id` | 产品 ID |
| `target_price` | 用户设置的目标价 |
| `price_type` | 价格类型，支持 `official`、`used`、`channel` |
| `status` | 提醒状态，当前使用 `ACTIVE` 和 `TRIGGERED` |
| `last_price` | 最近一次读取到的参考价 |
| `triggered_at` | 达到目标价的时间 |

`user_id + product_id + price_type` 使用唯一索引。同一个用户对同一产品的同一价格类型只保留一条提醒，重复保存会更新目标价和状态。

## 3. 后端接口

后端项目路径：

```text
/Users/a0000/IdeaProjects/digital-compass-backend
```

本模块新增接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/price-alerts` | 查询当前登录用户的全部价格提醒 |
| `GET` | `/api/price-alerts/product/{productId}` | 查询当前登录用户对某个产品的价格提醒 |
| `POST` | `/api/price-alerts` | 新增或更新价格提醒 |
| `DELETE` | `/api/price-alerts/{id}` | 删除当前登录用户的指定提醒 |

接口权限配置在：

```text
/Users/a0000/IdeaProjects/digital-compass-backend/src/main/java/com/a0000/digicompass/common/security/SecurityConfig.java
```

`/api/price-alerts` 和 `/api/price-alerts/**` 需要登录后访问。未登录请求返回 `401`。

## 4. 后端文件

| 文件 | 职责 |
| --- | --- |
| `modules/pricealert/controller/PriceAlertController.java` | 价格提醒 REST 接口 |
| `modules/pricealert/service/PriceAlertService.java` | 价格提醒服务接口 |
| `modules/pricealert/service/impl/PriceAlertServiceImpl.java` | 价格提醒业务实现 |
| `modules/pricealert/mapper/PriceAlertMapper.java` | 数据库访问 |
| `modules/pricealert/dto/PriceAlertItem.java` | 提醒列表返回结构 |
| `modules/pricealert/dto/PriceAlertSaveRequest.java` | 保存提醒请求结构 |

`PriceAlertSaveRequest` 对目标价和价格类型做参数校验：

- `productId` 必填。
- `targetPrice` 必填，且必须大于 0。
- `priceType` 支持 `official`、`used`、`channel`。

## 5. 状态判断

价格提醒状态依赖 `dc_price_reference` 中对应产品和价格类型的最新记录。

查询规则：

```text
按 product_id 和 price_type 查询 dc_price_reference
按 reference_date DESC、id DESC 取最新记录
参考价使用 COALESCE(avg_price, min_price, max_price)
```

状态判断规则：

| 条件 | 状态 |
| --- | --- |
| 没有参考价 | `ACTIVE` |
| 最新参考价大于目标价 | `ACTIVE` |
| 最新参考价小于或等于目标价 | `TRIGGERED` |

查询提醒列表和查询单个产品提醒时，服务层会刷新该提醒的 `last_price` 和 `status`。状态刷新使用提醒自身的 `id` 更新已有记录，避免产生跨用户数据写入。

## 6. 前端文件

前端项目路径：

```text
/Users/a0000/Projects/digital-compass-frontend
```

本模块涉及文件：

| 文件 | 职责 |
| --- | --- |
| `src/api/priceAlert.ts` | 封装价格提醒接口 |
| `src/types/priceAlert.ts` | 定义价格提醒类型 |
| `src/views/user/PriceAlertsView.vue` | 价格提醒列表页 |
| `src/views/user/ProductDetailView.vue` | 产品详情页提供价格提醒入口和设置弹窗 |
| `src/components/common/AppHeader.vue` | 登录后显示“价格提醒”导航入口 |
| `src/router/index.ts` | 添加 `/price-alerts` 路由并要求登录 |

## 7. 页面流程

用户在产品详情页点击“价格提醒”按钮。未登录用户会跳转到登录页；已登录用户会打开设置弹窗。

弹窗中输入目标价并保存后，前端调用：

```text
POST /api/price-alerts
```

保存成功后，产品详情页重新读取当前产品的提醒状态。若达到目标价，按钮显示“已达到目标价 ¥目标价”。

用户进入 `/price-alerts` 时，页面展示提醒列表。每条提醒显示：

- 产品图片
- 产品名称
- 品牌和分类
- 目标价
- 当前参考价
- 提醒状态
- 查看产品入口
- 删除按钮

## 8. 验证记录

已执行后端专项测试：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw test -Dtest=DatabaseSchemaFilesTests,PriceAlertControllerTests
```

结果：

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

已执行后端全量测试：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw test
```

结果：

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
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

已执行浏览器流程验证：

| 验证项 | 结果 |
| --- | --- |
| 未登录访问 `/price-alerts` 跳转登录页 | 通过 |
| 产品详情页打开价格提醒弹窗 | 通过 |
| 保存目标价后详情页显示提醒状态 | 通过 |
| `/price-alerts` 显示产品、目标价、当前参考价和状态 | 通过 |
| 点击“查看产品”跳转产品详情页 | 通过 |
| 删除提醒后列表显示空状态 | 通过 |
| 控制台错误 | 0 |
| 4xx/5xx 响应 | 0 |

浏览器验证产品为 `iPhone 16 Pro`。验证时使用临时官方参考价，验证结束后已经清理临时数据。
