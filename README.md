# DigiCompass 数码罗盘

DigiCompass 是一个面向数码产品选购场景的决策辅助平台，包含用户端产品浏览、对比、收藏、价格提醒、价格行情和 AI 选购助手，也包含管理端产品资料、分类品牌、用户、AI 接入配置和知识库维护能力。

项目采用前后端分离结构：

- `backend`：Spring Boot 后端服务
- `frontend`：Vue 3 前端应用

## 功能概览

用户端：

- 产品库与产品详情
- 多产品对比
- 收藏与浏览历史
- 价格提醒与价格行情
- 用户偏好设置
- AI 选购助手

管理端：

- Dashboard 统计概览
- 产品、参数、价格、购买链接、图片维护
- 分类与品牌维护
- 用户管理
- AI 供应商配置
- AI 知识库与工作流日志

## 技术栈

后端：

- Spring Boot 3
- Spring Security
- JSON Web Token
- MySQL
- JDBC
- 阿里云 OSS
- OpenAI Compatible API 接入

前端：

- Vue 3
- Vite
- TypeScript
- Pinia
- Vue Router
- Axios
- Lucide Icons

## AI 与 RAG

平台的 AI 选购助手采用“需求解析、产品检索、知识召回、推荐生成”的流程。管理端可以维护 AI 供应商配置和知识库内容，后端通过产品资料、参数、价格、标签和知识切片为推荐流程提供上下文。

当前项目支持 OpenAI Compatible 协议的模型接口，API Key 通过后端加密后保存。

## 配置说明

后端默认读取本地 MySQL 数据库 `digital_compass`。OSS、JWT 和 AI 配置支持通过环境变量覆盖，避免把真实密钥写入源码。

数据库结构与初始化数据位于：

- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/resources/db/migration`
- `backend/src/main/resources/db/seed*.sql`

## 当前状态

当前阶段已完成核心功能模块、权限控制、产品状态管理、价格提醒站内闭环、AI 配置管理和 RAG 知识库基础能力。后端测试与前端构建已通过。
