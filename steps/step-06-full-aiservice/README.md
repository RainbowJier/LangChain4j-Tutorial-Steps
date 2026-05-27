# Step 06: 完整 AiService 装配

## 学习目标

- 将 Step 01-05 的所有概念整合到单个 `AiServices.builder()` 调用中
- 理解各组件如何协同工作
- 建立从纯 Java 到 Spring Boot 的概念桥梁

## 前置条件

- 完成 [Step 01](../step-01-hello-llm/) ~ [Step 05](../step-05-memory-session/)

## 核心概念

### 组件整合

```java
DocAssistant assistant = AiServices.builder(DocAssistant.class)
    .chatModel(chatModel)                      // Step 01: LLM 调用
    .streamingChatModel(streamingModel)         // Step 02: 流式输出
    .contentRetriever(retriever)                // Step 03: RAG 检索
    .tools(knowledgeTool, taskTool)             // Step 04: Agent 工具
    .chatMemoryProvider(memoryProvider)         // Step 05: 会话记忆
    .build();
```

### 从 main() 到 @Configuration

| 本步骤（纯 Java） | Spring Boot 等价物 |
|------------------|-------------------|
| `main()` 中的手动组装 | `ChatConfig.java` 的 `@Bean` 方法 |
| `Map<String, ChatMemory>` | `ChatSessionManager.java` |
| `new AllMiniLmL6V2EmbeddingModel()` | `EmbeddingConfig.java` 的 `@Bean` |
| `new KnowledgeSearchTool()` | `@Component` + `@Tool` |

## 运行方式

```bash
cd steps/step-06-full-aiservice
mvn compile exec:java
```

## 你会看到什么

一个功能齐全的命令行聊天，支持：
- RAG 检索（基于知识库回答）
- 工具调用（搜索知识库、查询任务状态）
- 会话记忆（记住你说过的话）
- 流式输出（逐字显示）

## 与上一步的区别

- Step 05：只有记忆，没有 RAG 和工具
- **Step 06**：所有组件整合，形成完整的 AI 应用

## 下一步

[Step 07: Spring Boot REST API](../step-07-spring-boot-api/) — 把 main() 重构为 Spring Boot 多模块架构
