# DigiCompass 选购助手聊天模块记录

记录日期：2026-07-11

## 1. 模块目标

选购助手聊天模块用于让登录用户通过自然语言描述预算、用途和偏好，系统根据产品库数据返回推荐结果。当前版本采用规则推荐工作流，推荐结果以产品卡片形式展示，卡片可以跳转到产品详情页，也可以加入产品对比。

聊天记录保存在 MySQL 数据库中。每条会话绑定登录用户 ID，用户只能读取自己的会话和消息。

## 2. 数据表

本模块新增三张表：

| 表名 | 说明 |
| --- | --- |
| `dc_assistant_conversation` | 选购助手会话表，保存用户会话标题和更新时间 |
| `dc_assistant_message` | 选购助手消息表，保存用户消息和助手回复 |
| `dc_assistant_recommendation` | 选购助手推荐结果表，保存推荐产品卡片快照 |

建表脚本：

```text
/Users/a0000/IdeaProjects/digital-compass-backend/src/main/resources/db/migration/V5__assistant_chat.sql
```

当前本地数据库已经执行该脚本。

## 3. 后端接口

后端项目路径：

```text
/Users/a0000/IdeaProjects/digital-compass-backend
```

本模块新增或调整接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/assistant/conversations` | 查询当前登录用户的会话列表 |
| `POST` | `/api/assistant/conversations` | 创建当前登录用户的新会话 |
| `GET` | `/api/assistant/conversations/{id}/messages` | 查询指定会话的消息和推荐结果 |
| `POST` | `/api/assistant/conversations/{id}/messages` | 发送用户消息并生成助手回复 |
| `POST` | `/api/assistant/recommend` | 保留原有一次性推荐接口 |

聊天接口通过 JWT 识别当前用户，不从前端接收 `userId`。当用户访问其他人的会话时，后端返回 `403`。

## 4. 推荐工作流

当前版本未接入大模型，推荐逻辑由后端规则完成。

一次消息发送的处理流程：

```text
校验会话归属
保存用户消息
解析预算数字
读取产品库列表
读取产品详情、标签和价格
计算匹配分
生成推荐理由和风险提示
保存助手消息
保存推荐产品卡片快照
返回消息和推荐结果
```

匹配分主要参考：

- 产品库评分
- 用户输入中的预算
- 用户输入中的品类词，例如手机、电脑、平板
- 产品标签，例如拍照、影像、游戏、办公、轻薄、续航
- 官方价和二手参考价

当用户输入中包含明确品类词时，推荐工作流会先按品类过滤，再在同品类产品中排序。例如输入包含“手机”时，推荐结果只从手机分类中产生。

## 5. 前端文件

前端项目路径：

```text
/Users/a0000/Projects/digital-compass-frontend
```

本模块涉及文件：

| 文件 | 职责 |
| --- | --- |
| `src/api/assistant.ts` | 封装选购助手接口 |
| `src/types/assistant.ts` | 定义会话、消息、推荐卡片类型 |
| `src/views/user/AssistantView.vue` | 聊天式选购助手页面 |
| `src/components/assistant/AssistantProductCard.vue` | 助手推荐产品卡片 |
| `src/router/index.ts` | 将 `/assistant` 设置为需要登录 |

## 6. 页面流程

用户进入 `/assistant` 时，前端先读取当前用户的会话列表。若存在会话，默认打开最近更新的会话；若从首页搜索框带 `q` 参数进入，页面会创建新会话并自动发送该需求。

聊天区展示两类消息：

- `USER`：用户输入内容。
- `ASSISTANT`：系统生成的选购建议。

助手消息下方展示推荐产品卡片。卡片展示产品图、品牌、分类、官方价、二手参考价、匹配分、推荐理由和风险提示。点击卡片进入 `/products/{id}`，点击“加入对比”会复用现有产品对比模块。

## 7. 验证记录

已执行后端聊天模块单测：

```bash
cd /Users/a0000/IdeaProjects/digital-compass-backend
./mvnw -Dtest=AssistantChatTests test
```

结果：

```text
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
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
