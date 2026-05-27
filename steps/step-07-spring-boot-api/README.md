# Step 07 - Spring Boot REST API 多模块架构

## 概述

本项目是 Step 06 的 Spring Boot 版本，演示如何将纯 Java 的 LangChain4j 应用
演变为一个标准的 Spring Boot 多模块 Web 服务。

## 与 Step 06 的对照关系

| Step 06 (Plain Java)            | Step 07 (Spring Boot)                     |
|---------------------------------|-------------------------------------------|
| `new ZhipuAiChatModel(...)`    | `@Bean ChatModel` (IoC 容器管理)          |
| `new InMemoryEmbeddingStore<>()`| `@Bean EmbeddingStore` (单例 Bean)        |
| `EmbeddingStoreIngestor.builder()` | `@Service DocumentIngestionService`   |
| `EmbeddingStoreContentRetriever`  | `@Service RetrievalService`             |
| `ConcurrentHashMap` session map   | `@Component ChatSessionManager`         |
| `AiServices.builder()`          | `@Configuration ChatConfig` (声明式装配)  |
| `main()` 方法直接调用           | `@RestController` 暴露 REST/SSE API      |
| `application.properties` 手动读 | `application.yml` + `@Value` 自动注入    |

## 模块结构

```
smartdoc-llm/      -- LLM 提供商配置（智谱、DeepSeek、OpenAI）
smartdoc-rag/      -- RAG 管道（文档摄入、向量化、检索）
smartdoc-tools/    -- Agent 工具（知识搜索、任务状态查询）
smartdoc-chat/     -- 聊天装配（AiService、会话管理）
smartdoc-api/      -- REST API（SSE 流式、文档上传、会话管理）
```

## 快速启动

```bash
# 1. 设置 API Key 环境变量
export ZHIPU_API_KEY=your_api_key_here

# 2. 编译
mvn clean install -DskipTests

# 3. 启动（从 smartdoc-api 模块）
cd smartdoc-api && mvn spring-boot:run

# 4. 测试
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好", "sessionId": "test-001"}'
```

## API 端点

- `POST /api/chat` - SSE 流式对话
- `GET /api/chat/history/{sessionId}` - 获取历史记录
- `DELETE /api/chat/session/{sessionId}` - 清除会话
- `POST /api/documents/upload` - 上传文档（PDF/DOCX/TXT）
- `GET /api/documents` - 列出已上传文档
