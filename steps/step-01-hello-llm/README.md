# Step 01: Hello LLM

## Learning Target

- Understand `ChatModel` interface — the underlying API for interaction between LangChain4j and LLM.
- Understand `AiServices` — The declare AI Service.
- Understand `@SystemMessage` — define AI role and behavior rules
- Experience LLM's stateless characteristic (for Step 05 memory)

## Prerequisites

- Finish [Step 00: Environment Configuration](../step-00-setup/)
- API Key configured.

## Core Concepts

### ChatModel vs AiServices

```
ChatModel          underlying API, Send String, Receive String
                     ↓ More advanced encapsulation
AiServices         Declare AI Service, support @SystemMessage, @Tool, @MemoryId and other annotations
```

**Why is AiServices recommended**

- Define the behavior using annotations instead of code. (`@SystemMessage` defines the role)
- When adding functions such as tools and memory in the subsequent steps, simply add annotations.
- Interface definition is the "call contract" for LLM.

### @SystemMessage

```java

@SystemMessage("You area a seasoned Java developer mentor, Your response should be concise and accurate, and include code examples.")
interface MentorAssistant {
    String chat(String message);
}
```

The content of `@SystemMessage`  will be sent to LLM as a system promot, which influence its response style and
behavior.

## Operating mode

```bash
cd steps/step-01-hello-llm
mvn compile exec:java
```

## What you will see

The program demonstrates three invocation methods in sequence, then enters interactive conversation:

```
=== Method A: Direct ChatModel Call ===
Ask: Introduce Java in one sentence
Answer: Java is an object-oriented programming language...

=== Method B: AiServices Interface (without @SystemMessage) ===
Ask: What is RAG?
Answer: RAG (Retrieval-Augmented Generation) is a...

=== Method C: AiServices Interface (with @SystemMessage) ===
Ask: What is RAG?
Answer: RAG stands for Retrieval-Augmented Generation, a technique that combines external knowledge bases...
```

Notice the difference in response style between Method B and C for the same question — this is the effect of `@SystemMessage`.

## Key API

| API                         | Purpose                                               |
|-----------------------------|------------------------------------------------------|
| `ChatModel.chat(String)`    | Underlying invocation, send messages and receive response |
| `AiServices.builder(Class)` | Build declared AI service                            |
| `@SystemMessage`            | Define system prompts (AI role definition)           |

## Exercises

- [ ] Modify `@SystemMessage` to make AI respond in a specific style (e.g., only with emojis, only in English)
- [ ] Try `.temperature(0.0)` and `.temperature(1.5)` in `OpenAiChatModel.builder()`, compare the response differences

### What is temperature

`temperature` controls the randomness of the LLM response:

- **0.0**: Deterministic(确定性), same answer every time, good for factual accuracy.
- **1.0**: Balanced, normal randomness with good creativity and accuracy.
- **1.5+**: High randomness, more creative but less consistent.

Lower temperature = more predictable, higher temperature = more creative.

## Next Step

[Step 02: Streaming Output](../step-02-streaming/) — Make LLM responses appear character by character like a typewriter
