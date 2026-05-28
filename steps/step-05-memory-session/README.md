# Step 05: Session Memory

## Learning Target

1. Understand the stateless LLM (why memory is needed)
2. Understand the `ChatMemory` and `MessageWindow`
3. Understand the `@MemoryId` and `ChatMemoryProvider`
4. Understand the principle of multi-session isolation

## Prerequisites

- Complete [Step 01: Hello LLM](../step-01-hello-llm/)

## Core Concepts

### Problem: LLM is Stateless

```
Conversation from Step 01:
  You: My name is Xiaoming
  AI: Hello Xiaoming!
  You: What is my name?
  AI: Sorry, I don't know your name...  ← Forgot!
```

### Solution: ChatMemory

```java
AiServices.builder(MemoryAssistant.class)
    .chatModel(chatModel)
    .chatMemoryProvider(memoryProvider)   // ← Inject memory
    .build();
```

### Key Components

| Component                 | Purpose                                                   |
|---------------------------|-----------------------------------------------------------|
| `ChatMemory`              | Store conversation history                                |
| `MessageWindowChatMemory` | Sliding window implementation, keeps recent N messages    |
| `@MemoryId`               | Annotate session ID, distinguish different users/sessions |
| `ChatMemoryProvider`      | Create independent ChatMemory for each sessionId          |
| `InMemoryChatMemoryStore` | In-memory storage (can use database in production)        |

### Multi-Session Isolation

```
user-A's memory: [My name is Xiaoming, AI: Hello Xiaoming]
user-B's memory: [My name is Xiaohong, AI: Hello Xiaohong]
                  ↑ No interference between each other
```

## What You Will See

```
=== Option A: No Memory (Problem Demo) ===
Message 1: My name is Xiaoming
AI reply: Hello Xiaoming! How can I help you?
Message 2: What is my name?
AI reply: Sorry, I don't know your name...  ← No memory

=== Option B: With Memory (ChatMemory) ===
User A: What is my name? What do I do?
AI: Your name is Xiaoming, and you are a Java developer.  ← Remembered!
```

## Difference from Previous Step

- Step 04: LLM can call tools, but doesn't remember what was said before
- **Step 05**: After adding `chatMemoryProvider`, LLM can remember conversation history

## Exercises

- [ ] Change `maxMessages` to 4, have more than 5 rounds of conversation, observe if the earliest messages are forgotten
- [ ] Think: How to implement "clear session memory" functionality?

## Next Step

[Step 06: Complete AiService Assembly](../step-06-full-aiservice/) — Combine all concepts together