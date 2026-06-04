# Step 08 — Vue 3 Frontend

Build a browser-based chat UI that connects to the Step 07 Spring Boot backend, featuring streaming AI responses,
session management, and document upload.

---

## Learning Objectives

- Build a single-page application with **Vue 3 Composition API** and `<script setup>`
- Manage global state with **Pinia** (the Vue 3 state management library)
- Implement **Server-Sent Events (SSE)** streaming client using `fetch()` + `ReadableStream`
- Use **Element Plus** component library for UI layout and controls
- Render AI Markdown responses with **MarkdownIt**
- Handle file uploads with **FormData** and multipart requests
- Integrate with all backend API endpoints (chat, history, session management, documents)

---

## Project Structure

```
frontend/
├── index.html                  # HTML entry — mounts Vue app to #app
├── package.json                # Dependencies and scripts
├── vite.config.ts              # Vite dev server + proxy config
├── tsconfig*.json              # TypeScript configuration
└── src/
    ├── main.ts                 # App entry — creates Vue instance, registers plugins
    ├── App.vue                 # Root component — renders <router-view>
    ├── router/index.ts         # URL-to-component mapping
    ├── types/index.ts          # TypeScript interfaces (ChatMessage, ChatSession, etc.)
    ├── api/chat.ts             # Backend API client (SSE streaming, upload, history)
    ├── stores/chat.ts          # Pinia store (sessions, messages, streaming state)
    ├── views/ChatView.vue      # Page layout — sidebar + chat area + input
    ├── assets/main.css         # Global styles
    └── components/
        ├── SessionList.vue     # Left sidebar — session list, create/delete
        ├── MessageList.vue     # Chat messages — Markdown rendering, auto-scroll
        ├── MessageInput.vue    # Text input — Enter to send, Shift+Enter for newline
        └── DocUpload.vue       # Document upload + document list popover
```

---

## Quick Start

```bash
# 1. Install dependencies
cd frontend
npm install

# 2. Start dev server (requires Step 07 backend on localhost:8080)
npm run dev

# 3. Open http://localhost:5173
```

The Vite dev server proxies `/api/*` requests to `http://localhost:8080` (configured in `vite.config.ts`), so no CORS
configuration is needed during development.

---

## Architecture

### Data Flow

```
User types message → MessageInput
    → chatStore.sendMessage()    [stores/chat.ts]
    → streamChat() API           [api/chat.ts]
    → POST /api/chat (fetch + ReadableStream)
    → onToken callback → update store
    → Vue reactivity → MessageList re-renders
```

### Component Tree

```
App.vue
└── <router-view>
    └── ChatView.vue (layout)
        ├── SessionList.vue   (left sidebar: sessions)
        ├── MessageList.vue   (center: messages)
        └── ChatFooter
            ├── DocUpload.vue  (upload button + document list)
            └── MessageInput.vue (text input + send button)
```

### Backend API Integration

| Frontend Function  | HTTP Method | Backend Endpoint                | Purpose             |
|--------------------|-------------|---------------------------------|---------------------|
| `streamChat()`     | POST        | `/api/chat`                     | SSE streaming chat  |
| `getChatHistory()` | GET         | `/api/chat/history/{sessionId}` | Load past messages  |
| `clearSession()`   | DELETE      | `/api/chat/session/{sessionId}` | Delete a session    |
| `uploadDocument()` | POST        | `/api/documents/upload`         | Upload a file       |
| `listDocuments()`  | GET         | `/api/documents`                | List uploaded files |

---

## Core Concepts

### SSE Streaming (Server-Sent Events)

The backend uses Spring Boot's `SseEmitter` to push AI tokens one by one. The frontend reads the stream with `fetch()` +
`ReadableStream` (not `EventSource`, because we need POST):

```
fetch POST /api/chat
  ← data:Hello
  ← data:  world
  ← data:!
  ← data:[DONE]
```

The `streamChat()` function in `api/chat.ts` handles:

- Decoding `Uint8Array` chunks via `TextDecoder`
- Buffering partial lines across reads
- Parsing SSE `data:` prefix for each token
- Detecting `[DONE]` termination signal
- `AbortController` for the "stop generating" button

### Pinia State Management

The chat store (`stores/chat.ts`) manages:

| State              | Type                         | Purpose                   |
|--------------------|------------------------------|---------------------------|
| `sessions`         | `ChatSession[]`              | All conversation sessions |
| `currentSessionId` | `string`                     | Currently active session  |
| `messages`         | `Map<string, ChatMessage[]>` | Messages per session      |
| `isLoading`        | `boolean`                    | Whether AI is generating  |
| `documents`        | `string[]`                   | Uploaded document list    |

Key actions:

- `createSession()` — start a new chat
- `selectSession(id)` — switch sessions + load history from backend
- `deleteSession(id)` — remove session + notify backend
- `sendMessage(content)` — send message + start SSE stream
- `stopStreaming()` — abort the current SSE request

### TypeScript Interfaces

Defined in `types/index.ts`:

- **ChatMessage** — single message with `id`, `role` (user/assistant), `content`, `timestamp`, `isStreaming` flag
- **ChatSession** — conversation with `id`, `title`, `createdAt`, `lastMessageAt`
- **ChatRequest** — payload sent to backend: `{ message, sessionId }`

---

## Key Technologies

| Technology   | Role                           | Version |
|--------------|--------------------------------|---------|
| Vue 3        | UI framework (Composition API) | 3.5     |
| Vite         | Build tool & dev server        | 8.0     |
| TypeScript   | Type safety                    | 6.0     |
| Pinia        | State management               | 3.0     |
| Element Plus | UI components                  | 2.13    |
| MarkdownIt   | Markdown rendering             | 14.1    |

---

## Step 07 ↔ Step 08 Mapping

| Step 07 (Backend)             | Step 08 (Frontend)                   |
|-------------------------------|--------------------------------------|
| `ChatController` SSE endpoint | `api/chat.ts` → `streamChat()`       |
| `DocumentController` upload   | `DocUpload.vue` → `uploadDocument()` |
| `ChatSessionManager`          | `stores/chat.ts` session state       |
| `SseEmitter` event stream     | `ReadableStream` + `TextDecoder`     |
| `@RestController` response    | `fetch()` + `response.json()`        |

---

## Build for Production

```bash
npm run build    # TypeScript check + Vite build → dist/
npm run preview  # Preview the production build locally
```

---

## Difference from Step 07

- Step 07: Spring Boot backend with REST + SSE endpoints, no UI
- **Step 08**: Browser-based chat UI that consumes Step 07's API, adds session management, Markdown rendering, and
  document upload UI
