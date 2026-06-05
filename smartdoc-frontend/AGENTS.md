# AGENTS.md

## Project Type
Vue 3 + TypeScript SPA (Vite 8, Pinia 3, Vue Router 5) — chat UI for a LangChain4j Spring Boot backend.

## Commands
- **Dev**: `npm run dev` (proxies `/api/*` → `localhost:8080`)
- **Build**: `npm run build` (type-check + vite build → `dist/`)
- **Type-check**: `npm run type-check` (vue-tsc)
- **Lint**: `npm run lint` (oxlint + eslint)
- **Requires**: Node >=20.19, backend on `:8080`

## Architecture
- `src/main.ts` mounts App with Pinia + Vue Router
- SPA: single real route (`/` → ChatView), catch-all 404
- SSE streaming: `fetch POST /api/chat` + `ReadableStream` (not EventSource)
- Backend wraps all API responses in `{code, msg, data}` — `parseResponse()` in `api/chat.ts` unwraps it
- `@/` path alias → `src/`
- `noUncheckedIndexedAccess: true` in tsconfig (check for undefined on array access)

## Structure
| Layer | Key File | Role |
|-------|----------|------|
| API | `src/api/chat.ts` | SSE chat, history, session CRUD |
| Store | `src/stores/chat.ts` | Pinia: sessions, messages, streaming state |
| Types | `src/types/index.ts` | ChatMessage, ChatSession, ChatRequest, ApiResponse |
| Views | `src/views/ChatView.vue` | Layout: sidebar + topbar + chat area |
| Components | `src/components/*.vue` | SessionList, MessageList, MessageInput |

## Key Facts
- **No tests, no CI** — no test framework installed, no `.github/workflows/`
- **No frontend/ subdirectory** — `src/` is at project root
- CSS custom properties design system in `DESIGN.md` (Apple-inspired, 500+ lines)
- Inter font from Google Fonts
- CodeGraph indexed (13 files)
