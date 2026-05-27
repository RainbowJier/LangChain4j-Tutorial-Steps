# final/ — 完整项目参考实现

本目录是教学项目的最终形态，整合了 Step 01-09 中所有概念。

## 项目说明

这是一个「智能文档助手」，完整实现了：
- **RAG 检索增强生成**：上传文档，基于文档内容回答问题
- **Agent 工具调用**：知识库搜索、任务状态查询
- **流式输出**：SSE 实时推送 AI 回复
- **多会话管理**：支持多用户独立会话

## 架构

```
Vue 3 前端 (5173)
    │  SSE + REST
    ▼
Spring Boot 后端 (8080)
    ├── smartdoc-api      — REST API（SSE 流式、文档上传）
    ├── smartdoc-chat     — Chat 装配（AiService、会话管理）
    ├── smartdoc-tools    — Agent 工具（知识搜索、任务查询）
    ├── smartdoc-rag      — RAG 管道（文档摄入、向量化、检索）
    └── smartdoc-llm      — LLM 配置（多提供商支持）
```

## 快速开始

```bash
# 后端
cd final/backend
mvn clean install -DskipTests
cd smartdoc-api
mvn spring-boot:run

# 前端（另一个终端）
cd final/frontend
npm install
npm run dev

# 打开 http://localhost:5173
```

## 与教学步骤的对照

| 模块 | 对应步骤 | 核心概念 |
|------|---------|---------|
| smartdoc-llm | Step 01 | ChatModel、多提供商配置 |
| smartdoc-rag | Step 03 | Embedding、向量存储、文档摄入 |
| smartdoc-tools | Step 04 | @Tool 注解、Agent 工具调用 |
| smartdoc-chat | Step 05-06 | ChatMemory、AiService 完整装配 |
| smartdoc-api | Step 07 | REST API、SSE 流式传输 |
| frontend | Step 08 | Vue 3、Pinia、SSE 客户端 |

## 配置

编辑 `backend/smartdoc-api/src/main/resources/application.yml`：

```yaml
llm:
  provider: zhipu                # zhipu | deepseek | openai
  zhipu:
    api-key: YOUR_API_KEY
```
