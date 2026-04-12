# GeoSmart-Agent MVP Design

**Date:** 2026-04-12
**Status:** Approved
**Phase:** MVP Prototype

## Overview

GeoSmart-Agent 是一个面向国土空间平台的智能助手，基于 Spring Boot + LangChain4j 构建，具备政策咨询（RAG）和业务办理（Agent 工具调用）两大核心能力。

MVP 目标：跑通"文档上传 → RAG 检索 → Agent 工具调用 → 流式回答"的完整流程，用模拟数据和示例文档验证架构可行性。

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Project phase | MVP prototype | Get core flows working first |
| LLM strategy | Pluggable/multi-model | Start with one, switch via config |
| Vector database | In-Memory first | No infra overhead, swap later |
| Frontend | Vue 3 + Element Plus | Familiar for Chinese enterprise projects |
| Agent tools | Regulation search, Spatial query, Business status | Three core tools covering key scenarios |
| Knowledge data | Sample documents | Validate RAG pipeline with example docs |
| Dev environment | Local dev, containerize later | Fastest iteration for MVP |
| Architecture | Modular monolith | Clear boundaries, can evolve to microservices |

## Architecture

```
Vue 3 Frontend (SSE/REST)
        │
Spring Boot Backend
├── api/        — REST + SSE endpoints
├── chat/       — Session management, context orchestration
├── rag/        — Document ingestion, retrieval, generation
├── agent/      — Tool registration and dispatch
├── llm/        — Pluggable LLM abstraction
└── config/     — Global configuration
```

**Design principles:**
- `api/` handles routing and response formatting only, no business logic
- `chat/` manages dialog context, decides RAG vs Agent tool path
- `rag/` and `agent/` are independent, orchestrated by `chat/`
- `llm/` abstracts model provider differences, switch via configuration

## Backend Modules

### LLM Abstraction Layer (`llm/`)

Uses LangChain4j's `ChatLanguageModel` interface. Model provider selected via `application.yml`:

```yaml
llm:
  provider: deepseek  # or openai, gemini
```

A single `@Configuration` class creates the appropriate model bean based on the provider setting. No code changes needed to switch models.

### RAG Module (`rag/`)

**Data flow:** Document → Clean → Chunk (500-1000 chars) → Embed → In-Memory Store → Retrieve → Rerank → Inject into Prompt

| Component | Responsibility | LangChain4j Component |
|-----------|---------------|----------------------|
| `DocumentIngestionService` | Load PDF/Word, clean, chunk | `DocumentSplitters` |
| `EmbeddingService` | Text vectorization | `EmbeddingModel` |
| `RetrievalService` | Similarity search | `EmbeddingStoreIngestor` + `EmbeddingStore` |
| `RagService` | Orchestrate full RAG pipeline | `AiServices` + `@UserMessage` |

MVP uses In-Memory EmbeddingStore. The `EmbeddingStore` interface allows seamless swap to pgvector/Milvus later.

### Agent Module (`agent/`)

Three `@Tool` methods with mock data for MVP:

```java
@Tool("Search land spatial planning regulations by keyword")
String searchRegulations(@P("search keyword") String keyword);

@Tool("Query spatial planning info for a given area")
String querySpatialInfo(@P("area name or coordinates") String location);

@Tool("Query business processing status")
String queryBusinessStatus(@P("business ID") String businessId);
```

Tools return structured mock data. Future: integrate with real business system APIs.

### Chat Module (`chat/`)

```java
@AiService
interface GeoSmartAssistant {
    @SystemMessage("You are a land spatial planning intelligent assistant...")
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
```

- Returns `TokenStream` for SSE streaming output
- `ChatMemory` manages multi-turn context
- LangChain4j automatically decides when to call tools

### API Layer (`api/`)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/chat` | POST (SSE) | Streaming chat |
| `/api/chat/history/{sessionId}` | GET | Get conversation history |
| `/api/documents/upload` | POST | Upload knowledge base documents |
| `/api/documents` | GET | List uploaded documents |

## Frontend Design (Vue 3)

### Page Layout

```
┌─────────────────────────────────────────────┐
│  GeoSmart Agent              [Model Select] │
├──────────┬──────────────────────────────────┤
│          │                                  │
│  Session │        Chat Area (main)          │
│  List    │                                  │
│          │  AI/User messages with Markdown  │
│  ------  │  Tool call display cards         │
│  Sess 1  │                                  │
│  Sess 2  │                                  │
│          │  ┌──────────────────┬────────┐   │
│  [New]   │  │ Type message...  │ Send   │   │
│          │  └──────────────────┴────────┘   │
├──────────┴──────────────────────────────────┤
│  Knowledge Base: [Upload] [Uploaded: 3]     │
└─────────────────────────────────────────────┘
```

### Components

| Component | Responsibility |
|-----------|---------------|
| `ChatView` | Main chat page layout |
| `MessageList` | Message list with Markdown rendering |
| `MessageInput` | Input box + send button |
| `SessionList` | Left sidebar session history |
| `ToolCallDisplay` | Show Agent tool call process (MVP highlight) |
| `DocUpload` | Knowledge base document upload |

### Tech Stack

- **Build:** Vite
- **UI:** Element Plus
- **Markdown:** markdown-it + highlight.js
- **HTTP:** fetch + EventSource (SSE)
- **State:** Pinia

## Project Structure

```
GeoSmart-Agent/
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/geosmart/
│   │   ├── GeoSmartApplication.java
│   │   ├── api/
│   │   │   ├── ChatController.java
│   │   │   └── DocumentController.java
│   │   ├── chat/
│   │   │   ├── GeoSmartAssistant.java
│   │   │   └── ChatSessionManager.java
│   │   ├── rag/
│   │   │   ├── DocumentIngestionService.java
│   │   │   ├── RetrievalService.java
│   │   │   └── RagService.java
│   │   ├── agent/
│   │   │   ├── AgentConfig.java
│   │   │   └── tools/
│   │   │       ├── RegulationSearchTool.java
│   │   │       ├── SpatialQueryTool.java
│   │   │       └── BusinessStatusTool.java
│   │   ├── llm/
│   │   │   └── LlmConfig.java
│   │   └── config/
│   │       └── AppConfig.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── sample-docs/
│           ├── land-use-regulation.pdf
│           └── spatial-planning-guide.pdf
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.ts
│   │   ├── views/
│   │   │   └── ChatView.vue
│   │   ├── components/
│   │   │   ├── MessageList.vue
│   │   │   ├── MessageInput.vue
│   │   │   ├── SessionList.vue
│   │   │   ├── ToolCallDisplay.vue
│   │   │   └── DocUpload.vue
│   │   ├── api/
│   │   │   └── chat.ts
│   │   ├── stores/
│   │   │   └── chat.ts
│   │   └── types/
│   │       └── index.ts
│   └── index.html
└── README.md
```

## Tech Stack Summary

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 3.x |
| Java | JDK | 17+ |
| AI Framework | LangChain4j | 1.0+ |
| LLM | DeepSeek / OpenAI (switchable) | API |
| Vector Store | In-Memory (MVP) → pgvector/Milvus | - |
| Document Parsing | Apache PDFBox + Apache POI | - |
| Frontend | Vue 3 + TypeScript | 3.x |
| UI Library | Element Plus | - |
| Build | Vite (frontend) + Maven (backend) | - |
| Communication | REST + SSE | - |

## MVP Demo Flow

1. Upload sample regulation documents → auto-parse, chunk, embed
2. Ask "What is the planned use of Plot XX?"
3. System retrieves relevant regulations via RAG + queries spatial info via Agent tool
4. Streams comprehensive answer with tool call process displayed

## Future Evolution

- **Vector DB:** Swap In-Memory → pgvector/Milvus
- **Agent Tools:** Mock data → real business system API integration
- **Auth:** Add user authentication and role-based access
- **Containerization:** Docker Compose for all dependencies
- **Observability:** Logging, token usage tracking, latency metrics
- **GIS Integration:** Map visualization with spatial query results
