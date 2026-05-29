# LangChain4j Progressive Tutorial

Build AI applications from scratch — designed for experienced Java developers.

This project takes you through **10 progressive steps** to master LangChain4j core concepts: LLM calling, streaming output, RAG (Retrieval-Augmented Generation), Agent tool calling, session memory, and finally a complete Spring Boot + Vue 3 full-stack application.

---

## Learning Roadmap

```
Step 00 ─── Step 01 ─── Step 02 ─── Step 03 ─── Step 04 ─── Step 05 ─── Step 06
Setup       Hello LLM   Streaming   RAG         Tools       Memory      Full Assembly
  │                                                                       │
  └── Pure Java (no Spring Boot required) ────────────────────────────────┘
                                                                           │
Step 07 ─── Step 08 ─── Step 09
Spring Boot  Vue Frontend  Production
  │
  └── Spring Boot + Vue 3 Full Stack ──┘
```

- **Steps 00–06**: Pure Java, run with `mvn compile exec:java` — no Spring Boot needed
- **Step 07**: Spring Boot multi-module REST API
- **Step 08**: Vue 3 frontend with SSE streaming chat
- **Step 09**: Production-hardening (vector DB, monitoring, security)

---

## Steps Overview

Each step is an **independently runnable** Maven project. Click through for detailed READMEs with core concepts, code walkthroughs, and exercises.

| Step | Topic | Core Concepts | Run |
|------|-------|---------------|-----|
| [Step 00](LangChain4j-Tutorial-Steps/step-00-setup/) | Environment Setup | API Key configuration, provider selection, connectivity verification | `mvn compile exec:java` |
| [Step 01](LangChain4j-Tutorial-Steps/step-01-hello-llm/) | Hello LLM | `ChatModel`, `AiServices`, `@SystemMessage`, stateless LLM | `mvn compile exec:java` |
| [Step 02](LangChain4j-Tutorial-Steps/step-02-streaming/) | Streaming Output | `StreamingChatModel`, `TokenStream` callbacks (`onPartialResponse`, `onCompleteResponse`, `onError`) | `mvn compile exec:java` |
| [Step 03](LangChain4j-Tutorial-Steps/step-03-rag-retrieval/) | RAG Basics | Embedding, vector store, document chunking, `ContentRetriever`, overlap strategy | `mvn compile exec:java` |
| [Step 04](LangChain4j-Tutorial-Steps/step-04-tools/) | Agent Tools | `@Tool` / `@P` annotations, Function Calling workflow, tool registration | `mvn compile exec:java` |
| [Step 05](LangChain4j-Tutorial-Steps/step-05-memory-session/) | Session Memory | `ChatMemory`, `@MemoryId`, `ChatMemoryProvider`, multi-session isolation | `mvn compile exec:java` |
| [Step 06](LangChain4j-Tutorial-Steps/step-06-full-aiservice/) | Full AiService | `AiServices.builder()` integrating all components (RAG + Tools + Memory + Streaming) | `mvn compile exec:java` |
| [Step 07](LangChain4j-Tutorial-Steps/step-07-spring-boot-api/) | Spring Boot API | Multi-module architecture, `@Bean` declarative assembly, SSE endpoints, document upload | `mvn spring-boot:run` |
| [Step 08](LangChain4j-Tutorial-Steps/step-08-vue-frontend/) | Vue 3 Frontend | Composition API, Pinia state management, SSE client, Markdown rendering, Element Plus | `npm run dev` |
| [Step 09](LangChain4j-Tutorial-Steps/step-09-production/) | Production Optimization | Vector DB migration (Milvus), timeout/retry, circuit breaker, monitoring, security | `mvn compile exec:java` |

### Learning path recommendations

| Path | Steps | Time |
|------|-------|------|
| **Core concepts** (recommended) | 00 → 01 → 03 → 04 → 05 → 06 | ~2 hours |
| **Full-stack** | Core + 07 + 08 | ~4 hours |
| **Production** | Full-stack + 09 | ~5 hours |

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20.19+ (Step 08 and frontend only)
- An LLM API Key

### Configure LLM API Key

Edit `LangChain4j-Tutorial-Steps/step-00-setup/src/main/resources/application.yml`:

```yaml
llm:
  provider: deepseek          # Switch between: zhipu, deepseek
  deepseek:
    base-url: https://api.deepseek.com/
    api-key: sk-your-key-here
    model-name: deepseek-chat
  zhipu:
    base-url: https://open.bigmodel.cn/api/paas/v4/
    api-key: ${LLM_API_KEY:your-api-key-here}
    model-name: glm-4-flash
```

> **Tip**: Use the `LLM_API_KEY` environment variable instead of hardcoding keys. The config falls back to the env var when the placeholder is present.

### Provider Comparison

| Provider | Base URL | Free Tier | Best For |
|----------|----------|-----------|----------|
| **Zhipu GLM** | `open.bigmodel.cn/api/paas/v4/` | Yes (registration) | Chinese-language tasks, stable API |
| **DeepSeek** | `api.deepseek.com` | Yes (limited) | Code generation, reasoning |
| **OpenAI** | `api.openai.com` | No | General-purpose, broadest compatibility |

Zhipu GLM and DeepSeek both use OpenAI-compatible APIs, so `OpenAiChatModel` works with all of them — just change the `base-url` and `api-key`.

### Run Your First Step

```bash
cd LangChain4j-Tutorial-Steps/step-00-setup
mvn compile exec:java
```

You should see:

```
=== LangChain4j Teaching environment inspection ===
Configured Provider: deepseek
API Key: sk-abcd...
Model: deepseek-chat
Base URL: https://api.deepseek.com/

Connecting to LLM API... Connection successful!
LLM responses: OK

✅ Environment inspection passed! you are now already to start your study.
```

### Run All Steps

```bash
cd LangChain4j-Tutorial-Steps
mvn compile            # Compile all pure-Java steps (00–06)
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend Framework | Spring Boot + Java 17 | 3.5.0 |
| AI Framework | LangChain4j | 1.13.0 |
| Frontend | Vue 3 + TypeScript + Vite | 3.5 / 8.0 |
| UI Library | Element Plus | 2.13 |
| State Management | Pinia | 3.0 |
| Embedding Model | All-MiniLM-L6-v2 (ONNX) | Local inference |
| Vector Store (dev) | InMemoryEmbeddingStore | Built-in |
| Vector Store (prod) | Milvus / pgvector / Weaviate | External |
| LLM Providers | Zhipu GLM / DeepSeek / OpenAI | Swappable |

---

## Architecture

### Step-by-Step Concept Growth

```
Step 01:  ChatModel (single LLM call)
            ↓
Step 02:  + StreamingChatModel (token-by-token output)
            ↓
Step 03:  + ContentRetriever (RAG — answer from your documents)
            ↓
Step 04:  + Tools (LLM calls your Java methods via Function Calling)
            ↓
Step 05:  + ChatMemory (conversation history across turns)
            ↓
Step 06:  AiServices.builder() — all components assembled
            ↓
Step 07:  Spring Boot — @Bean / @Service / @RestController wrapping
            ↓
Step 08:  Vue 3 — browser chat UI with SSE streaming
            ↓
Step 09:  Production — Milvus, monitoring, security
```

### Key Design Principle: Concept Bridge

Step 06 (pure Java) and Step 07 (Spring Boot) are **structurally identical** — every component in `main()` has a direct `@Bean` counterpart:

| Step 06 (Plain Java) | Step 07 (Spring Boot) |
|----------------------|-----------------------|
| `new ZhipuAiChatModel(...)` | `@Bean ChatModel` |
| `new InMemoryEmbeddingStore()` | `@Bean EmbeddingStore` |
| `EmbeddingStoreIngestor.builder()` | `@Service DocumentIngestionService` |
| `ConcurrentHashMap` session map | `@Component ChatSessionManager` |
| `AiServices.builder()` | `@Configuration ChatConfig` |
| `main()` direct call | `@RestController` REST/SSE API |

This lets you learn the AI concepts without Spring Boot complexity first, then see how the same code maps to production-grade infrastructure.

---

## Development Workflow

### Validate All Steps

```bash
cd LangChain4j-Tutorial-Steps
mvn compile
```

This compiles all pure-Java modules (00–06). Step 07 has its own parent POM under `step-07-spring-boot-api`.

### Code Conventions

- **Package**: `com.smartdoc` in Spring Boot steps, `com.tutorial` in pure-Java steps
- **Module names**: `smartdoc-*` for Step 07 multi-module
- **Cross-step markers**: Comments like `// 【Step 06 对照】...` show mapping between steps
- **Business domain**: Smart document assistant (Java dev team handbook as example knowledge base)

---

## Modular Structure

```
LangChain4j-Tutorial-Steps/
├── pom.xml                          # Parent POM (LangChain4j 1.13.0, Java 17)
├── step-00-setup/                   # Environment verification
│   └── src/main/resources/application.yml  # LLM provider config
├── step-01-hello-llm/               # Basic LLM invocation
├── step-02-streaming/               # Token streaming
├── step-03-rag-retrieval/           # RAG pipeline
│   └── src/main/resources/knowledge-base.txt  # Sample document
├── step-04-tools/                   # Agent tool calling
├── step-05-memory-session/          # Conversation memory
├── step-06-full-aiservice/          # Full integration
├── step-07-spring-boot-api/         # Spring Boot multi-module
│   ├── smartdoc-llm/                #   LLM provider config
│   ├── smartdoc-rag/                #   RAG pipeline
│   ├── smartdoc-tools/              #   Agent tools
│   ├── smartdoc-chat/               #   AiService assembly
│   └── smartdoc-api/                #   REST controllers
├── step-08-vue-frontend/            # Vue 3 frontend
│   └── frontend/                    #   Vite + Vue 3 + Pinia
└── step-09-production/              # Production optimizations
```

---

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|-------------|----------|
| `Connection refused` or `401` | Missing or invalid API Key | Set `LLM_API_KEY` env var or check `application.yml` |
| Empty response from LLM | Provider model name mismatch | Verify `model-name` matches the provider's available models |
| Step 03: `knowledge-base.txt not found` | Wrong working directory | Run `mvn` from the step directory, not from root |
| Step 07: `Could not find smartdoc-llm` | Parent POM not installed | Run `mvn clean install -DskipTests` from `step-07-spring-boot-api/` first |
| Step 08: CORS errors | Backend not running | Start Step 07 backend before the frontend dev server |
| Streaming nothing printed | `CountDownLatch` missing | Remember to call `.start()` and wait for completion |

---

## Further Reading

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j)
- [Zhipu GLM Platform](https://open.bigmodel.cn/)
- [DeepSeek Platform](https://platform.deepseek.com/)
- [Milvus Vector Database](https://milvus.io/)

---

## License

MIT
