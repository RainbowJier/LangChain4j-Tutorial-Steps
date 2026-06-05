# AGENTS.md

## Project Identity
- Step 07 of the LangChain4j Progressive Tutorial — Spring Boot 3.5 (Java 17) multi-module REST API wrapping a LangChain4j 1.13.0 AI agent with RAG + Tools + Session Memory + SSE streaming.
- **6 Maven modules** with strict bottom-up dependency order: `common → chatModel → rag → tools → chat → api`

## Build & Run (order matters)
```bash
# 1. Install parent POM and all modules first (required — build fails without this)
cd smartdoc-backend
mvn clean install -DskipTests

# 2. Run the API server
cd smartdoc-api && mvn spring-boot:run    # port 8080

# 3. Set env vars before running
set LLM_PROVIDER=deepseek                  # or: zhipu
set DEEPSEEK_API_KEY=sk-...                # or: ZHIPU_API_KEY
# LLM_API_KEY overrides any provider-specific key
```
- `mvn spring-boot:run` must be run from `smartdoc-api`, not the parent. If run from parent it starts nothing.
- Server starts on **port 8080**.

## Architecture
| Module | Key Class(es) | Role |
|--------|---------------|------|
| `smartdoc-common` | `AjaxResult`, `ProviderEnum`, `ResultCodeEnum` | Shared DTOs, enums, API response wrapper |
| `smartdoc-chatModel` | `ChatModelConfig`, `ChatModelProperties`, `EmbeddingConfig` | LLM + embedding beans, config binding |
| `smartdoc-rag` | `DocumentIngestionService`, `RetrievalService` | PDF/DOCX/TXT ingestion, vector store content retriever |
| `smartdoc-tools` | `KnowledgeSearchTool`, `TaskStatusTool` | `@Tool` agent methods (both **return mock data**) |
| `smartdoc-chat` | `ChatConfig`, `ChatSessionManager`, `SpringCodeAssistant` | AiServices assembly, sliding-window session memory |
| `smartdoc-api` | `SmartDocApplication`, `ChatController`, `DocumentController`, `AppConfig` | **Main entrypoint**, REST/SSE controllers, CORS config |

- Entrypoint: `com.smartdoc.api.SmartDocApplication` — `@SpringBootApplication(scanBasePackages = "com.smartdoc")` with `@EnableScheduling`

## LLM Config
- Config: `smartdoc-api/src/main/resources/application.yml` — `llm.provider`, `llm.providers.{zhipu,deepseek}`
- All providers use **OpenAI-compatible** `OpenAiChatModel` / `OpenAiStreamingChatModel` (DeepSeek, Zhipu both supported)
- Embedding: `AllMiniLmL6V2EmbeddingModel` (local ONNX by default) or `ZhipuAiEmbeddingModel` — switch via `rag.embedding.type` (default `local`)
- Vector store: `InMemoryEmbeddingStore` — no external DB needed for development

## API Endpoints
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/chat` | SSE streaming — body `{"message","sessionId"}`. Returns `SseEmitter` with `data:[DONE]` termination |
| `GET` | `/api/chat/history/{sessionId}` | Returns `[{role, content}]` |
| `DELETE` | `/api/chat/session/{sessionId}` | Clears session memory |
| `POST` | `/api/documents/upload` | Multipart file upload (PDF/DOCX/TXT), max 50MB |
| `GET` | `/api/documents` | Lists uploaded document filenames |

## Key Quirks
- **Chat requires POST even for SSE** — because the message body is sent in the request. Frontend uses `fetch POST` + `ReadableStream`, not `EventSource`.
- **No tests exist** — `spring-boot-starter-test` is on classpath but there are zero test classes across all modules
- **Tools are stubs** — `KnowledgeSearchTool` and `TaskStatusTool` return hardcoded JSON; not backed by real services
- **Session eviction**: `ChatSessionManager` runs a `@Scheduled` task every 5 minutes, evicting sessions inactive >30 minutes
- **CORS**: `AppConfig` allows all origins, all standard methods, with credentials
- **GlobalExceptionHandler** in `smartdoc-api` maps validation errors (400), oversized uploads (413), and general errors (500)
- **System prompt**: `smartdoc-api/src/main/resources/prompts/spring-code-assistant.md` — Chinese-language Java-expert persona instructing the LLM to generate Mermaid diagrams

## Package Convention
- All modules use `com.smartdoc.*` — not `com.tutorial` (that's only for Steps 00–06 pure Java)
- Cross-step annotations in comments: `// 【Step 06 对照】...` map between plain-Java and Spring Boot versions
