# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在本仓库中工作时提供指导。

## 项目概述

GeoSmart-Agent 是面向国土空间规划的全栈智能助手。通过 RAG（检索增强生成）实现政策咨询，通过 Agent（智能体工具调用）实现业务办理。后端基于 Spring Boot 3.5 + LangChain4j 1.13，前端基于 Vue 3 + Vite 8。

## 常用命令

### 后端（在 `backend/` 目录下执行）

```bash
mvn compile                          # 编译
mvn test                             # 运行全部测试
mvn test -Dtest=TestClassName        # 运行单个测试类
mvn test -Dtest=TestClassName#method # 运行单个测试方法
mvn spring-boot:run                  # 启动开发服务器（端口 8080）
mvn clean package -DskipTests        # 打包 JAR（跳过测试）
```

### 前端（在 `frontend/` 目录下执行）

```bash
npm run dev          # 开发服务器 + HMR（端口 5173，/api 代理至 localhost:8080）
npm run build        # 类型检查 + 生产构建
npm run type-check   # 仅 TypeScript 类型检查（vue-tsc）
npm run lint         # 先运行 oxlint，再运行 eslint（自动修复）
npm run lint:eslint  # 仅 ESLint
npm run lint:oxlint  # 仅 Oxlint
```

Node 版本要求：`^20.19.0 || >=22.12.0`

### 全栈启动

1. 启动后端：`cd backend && mvn spring-boot:run`
2. 启动前端：`cd frontend && npm run dev`
3. 打开 http://localhost:5173（Vite 自动将 API 请求代理至后端）

## 架构

```
frontend (Vue 3, 端口 5173)
  │  SSE 流式传输 + REST API
  │  Vite 代理: /api → localhost:8080
  ▼
backend (Spring Boot, 端口 8080)
  ├── api/        — ChatController（SSE 流式）、DocumentController（上传/列表）
  ├── chat/       — GeoSmartAssistant（AiService 接口）、ChatConfig（组件装配）、ChatSessionManager
  ├── rag/        — DocumentIngestionService → EmbeddingConfig → RetrievalService
  ├── agent/tools/— @Tool 组件：RegulationSearchTool、SpatialQueryTool、BusinessStatusTool
  ├── llm/        — LlmConfig（可插拔 LLM 提供商）、LlmProperties
  └── config/     — AppConfig（示例数据自动加载）
```

### 核心集成点

- **ChatConfig.java** 是核心装配入口：构建 `AiServices`，注册工具、`ContentRetriever`（RAG）和 `ChatMemoryProvider`（会话管理）。新增工具或修改 RAG 配置时需修改此文件。
- **GeoSmartAssistant.java** 定义了 `@SystemMessage` 系统提示词和 `chat()` 接口 — 这是 LLM 的调用契约。
- **前端 Pinia store**（`stores/chat.ts`）管理 SSE 流式状态；原始 SSE 解析逻辑在 `api/chat.ts` 中。

### LLM 提供商切换

通过 `application.yml` 中的 `llm.provider` 控制（默认：`zhipu`）。可通过环境变量 `LLM_PROVIDER=zhipu|deepseek|openai` 覆盖。每个提供商需要对应的 `*_API_KEY` 环境变量。`LlmConfig.java` 根据提供商名称选择 `ChatModel` Bean。

### RAG 管道

文档（PDF/DOCX/TXT）→ 解析 → 递归分块（500 字符，100 重叠）→ 向量化（AllMiniLmL6-v2 ONNX，本地模型）→ InMemoryEmbeddingStore。检索使用 `EmbeddingStoreContentRetriever`，`maxResults: 5`。配置项在 `application.yml` 的 `rag.*` 下。

### Agent 工具

工具是 Spring `@Component` 类，方法上标注 LangChain4j 的 `@Tool` 注解。当前返回硬编码的模拟数据。新增工具时，在 `ChatConfig.tools(...)` 中注册 Bean 即可。

### 聊天流式传输

后端使用 `SseEmitter` 配合 LangChain4j 的 `TokenStream` API。前端通过 `fetch()` + `ReadableStream` 读取，解析 `data:{token}\n\n` 格式的 SSE 数据。

## 配置

所有配置集中在 `backend/src/main/resources/application.yml`。关键配置项：

- `llm.provider` / `llm.{provider}.api-key` — LLM 提供商选择及 API 密钥
- `rag.chunk-size`、`rag.chunk-overlap`、`rag.max-results` — RAG 参数调优
- `chat.max-memory-messages` — 单会话记忆窗口（默认 20 条）
- 示例文档自动加载目录：`backend/src/main/resources/sample-docs/`

## 测试

后端测试使用 JUnit 5 + AssertJ + Mockito + Spring Boot Test（`@SpringBootTest`、`@WebMvcTest`）。测试代码位于 `backend/src/test/java/com/geosmart/`，与源码包结构一一对应。

前端尚未安装测试框架。
