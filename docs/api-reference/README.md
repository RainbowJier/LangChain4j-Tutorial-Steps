# LangChain4j API 速查

## ChatModel 体系

```java
// 同步模型
ChatModel chatModel = OpenAiChatModel.builder()
    .baseUrl("https://api.openai.com/v1/")
    .apiKey("key")
    .modelName("gpt-4o-mini")
    .timeout(Duration.ofSeconds(30))
    .maxRetries(3)
    .temperature(0.7)
    .build();

// 流式模型
StreamingChatModel streaming = OpenAiStreamingChatModel.builder()
    .baseUrl("...")
    .apiKey("...")
    .modelName("...")
    .build();
```

## AiServices.builder()

```java
MyAssistant assistant = AiServices.builder(MyAssistant.class)
    .chatModel(chatModel)                    // 必需：同步 LLM
    .streamingChatModel(streamingModel)       // 可选：流式 LLM
    .contentRetriever(retriever)              // 可选：RAG 检索
    .tools(tool1, tool2)                      // 可选：Agent 工具
    .chatMemoryProvider(memoryProvider)       // 可选：会话记忆
    .build();
```

## RAG 相关

```java
// Embedding 模型（本地）
EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

// 向量存储
EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

// 文档摄入
EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
    .embeddingStore(store)
    .embeddingModel(embeddingModel)
    .documentSplitter(DocumentSplitters.recursive(500, 100))
    .build();
ingestor.ingest(document);

// 内容检索
ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
    .embeddingStore(store)
    .embeddingModel(embeddingModel)
    .maxResults(5)
    .build();
```

## @Tool 注解

```java
public class MyTool {
    @Tool("工具描述 — 给 LLM 看的，帮助它决定何时调用")
    public String myMethod(
            @P("参数描述 — 帮助 LLM 正确传参") String param) {
        return "结果";
    }
}
```

## 会话记忆

```java
// 接口定义
interface Assistant {
    String chat(@MemoryId String sessionId, @UserMessage String message);
}

// 记忆提供者
ChatMemoryProvider provider = memoryId ->
    MessageWindowChatMemory.builder()
        .id(memoryId.toString())
        .chatMemoryStore(store)
        .maxMessages(20)
        .build();

// 注入
AiServices.builder(Assistant.class)
    .chatModel(chatModel)
    .chatMemoryProvider(provider)
    .build();
```

## TokenStream（流式）

```java
interface StreamAssistant {
    TokenStream chat(String message);
}

assistant.chat("问题")
    .onPartialResponse(token -> System.out.print(token))
    .onCompleteResponse(response -> { /* 完成 */ })
    .onError(error -> { /* 错误 */ })
    .start();  // 必须调用
```
