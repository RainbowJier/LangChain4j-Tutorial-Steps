# Step 05: 会话记忆

## 学习目标

- 理解 LLM 的无状态特性（为什么需要记忆）
- 掌握 `ChatMemory`、`MessageWindowChatMemory`
- 掌握 `@MemoryId` 和 `ChatMemoryProvider`
- 理解多会话隔离的原理

## 前置条件

- 完成 [Step 01: 你好 LLM](../step-01-hello-llm/)（理解无状态问题）

## 核心概念

### 问题：LLM 是无状态的

```
Step 01 的对话：
  你: 我叫小明
  AI: 你好小明！
  你: 我叫什么？
  AI: 抱歉，我不知道你的名字...  ← 忘了！
```

### 解决方案：ChatMemory

```java
AiServices.builder(MemoryAssistant.class)
    .chatModel(chatModel)
    .chatMemoryProvider(memoryProvider)   // ← 注入记忆
    .build();
```

### 关键组件

| 组件 | 作用 |
|------|------|
| `ChatMemory` | 存储对话历史 |
| `MessageWindowChatMemory` | 滑动窗口实现，保留最近 N 条消息 |
| `@MemoryId` | 标注会话 ID，区分不同用户/会话 |
| `ChatMemoryProvider` | 为每个 sessionId 创建独立的 ChatMemory |
| `InMemoryChatMemoryStore` | 内存存储（生产环境可用数据库） |

### 多会话隔离

```
user-A 的记忆: [我叫小明, AI: 你好小明]
user-B 的记忆: [我叫小红, AI: 你好小红]
              ↑ 互不干扰
```

## 运行方式

```bash
cd steps/step-05-memory-session
mvn compile exec:java
```

## 你会看到什么

```
=== 方案 A：无记忆（问题演示） ===
第 1 句: 我叫小明
AI 回复: 你好小明！有什么我可以帮你的吗？
第 2 句: 我叫什么名字？
AI 回复: 抱歉，我不知道你的名字...  ← 没有记忆

=== 方案 B：有记忆（ChatMemory） ===
用户 A: 我叫什么？我是做什么的？
AI: 你叫小明，你是一名 Java 开发者。  ← 记住了！
```

## 与上一步的区别

- Step 04：LLM 能调用工具，但不记得之前说过什么
- **Step 05**：添加 `chatMemoryProvider` 后，LLM 能记住对话历史

## 练习

- [ ] 把 `maxMessages` 改为 4，连续对话 5 轮以上，观察最早的消息是否被遗忘
- [ ] 思考：如果要实现"清除会话记忆"功能，应该怎么做？

## 下一步

[Step 06: 完整 AiService 装配](../step-06-full-aiservice/) — 把所有概念组合到一起
