# Step 02: Stream Output

## Learning Target

- Understand `StreamingChatModel`: the streaming version of ChatModel.
- Master `TokenStream` callback model (`onPartialResponse` / `onCompleteResponse` / `onError`)
- Understand why stream output is needed (user experience + perceived(感知的) latency（延迟、潜在）)

## Prerequisites

- Finish [Step 01: Hello LLM](../step-01-hello-llm/)

## Core Concepts

### Synchronous vs Streaming

```
Synchronous (Step 01):   User asking -→ [wait 5 seconds] -→ Return the complete answer at once.
Streaming (Step 02):   User asking -→ 200ms later, the first charactor appears -→ once charactor at a time -→ complete
```

### TokenStream Callback Model

```java
TokenStream stream = assistant.chat("Question");

stream.onPartialResponse(token ->{
        // Each time a Token is received, it is displayed in real time.
        System.out.print(token);}
        )
        .onCompleteResponse(response ->{
        // All generated contented is ready, for cleaning and statistics purpose.
        })
        .onError(error ->{
        // Error — Used for error handling.
        })
        .start();  // initiate the streaming transmission.
```

### Stream Configuration in the AiServices

```java
// Step 01: ChatModel was used in synchronous mode.
AiServices.builder(Assistant .class)
    .chatModel(chatModel)

// Step 02: StreamingChatModel was used.
AiServices.builder(StreamingAssistant .class)
    .streamingChatModel(streamingChatModel)
```

The return type of interface should also be changed from `String` to `TokenStream`.

## Operation mode

```bash
cd steps/step-02-streaming
mvn compile exec:java
```

## What you will see

LLM's response will appear like a typewriter.

## Key API

| API                                | Purpose                                       |
|------------------------------------|-----------------------------------------------|
| `OpenAiStreamingChatModel`         | OpenAI-compatible implementation of streaming ChatModel |
| `TokenStream`                      | AiServices streaming return type              |
| `onPartialResponse(String)`        | Callback for each Token                       |
| `onCompleteResponse(ChatResponse)` | Callback when generation is complete          |
| `start()`                          | Must be called to start streaming transmission|

## Differences from the Previous Step

- Step 01: `ChatModel` → Synchronous, returns all at once
- Step 02: `StreamingChatModel` → Streaming, returns one token at a time

## Exercises

- [ ] Add a counter in `onPartialResponse` to count how many Tokens were generated.
  ```java
  AtomicInteger tokenCount = new AtomicInteger(0);
  stream.onPartialResponse(token -> {
      System.out.print(token);
      tokenCount.incrementAndGet();
  })
  .onCompleteResponse(response -> {
      System.out.println("\nTotal tokens: " + tokenCount.get());
  })
  .start();
  ```
- [ ] Think: Why does streaming need `CountDownLatch`? (Hint: Which thread does `onPartialResponse` execute on?)

**Answer:** `onPartialResponse` executes on a separate background thread (usually from the HTTP client's thread pool), not the main thread. Without `CountDownLatch` or similar synchronization mechanism, the main thread would continue execution immediately after calling `.start()`, potentially exiting the program before the streaming completes. `CountDownLatch.await()` blocks the main thread until `onCompleteResponse` or `onError` calls `countDown()`, ensuring the program waits for the streaming to finish.

## Next Step

[Step 03: RAG Retrieval-Augmented Generation](../step-03-rag-retrieval/) — Let LLM answer questions based on your own documents
