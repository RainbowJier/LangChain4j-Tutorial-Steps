# GeoSmart-Agent

面向国土空间规划的全栈智能助手。通过 RAG（检索增强生成）实现政策咨询，通过 Agent（智能体工具调用）实现业务办理。

**核心痛点：** 国土法规更新快、查询难，业务系统操作门槛高。
**解决方案：** RAG 解决知识储备 + Agent 解决业务执行。

## 功能特性

- **智能对话** — 基于 LLM 的自然语言交互，SSE 流式响应实时显示
- **政策咨询（RAG）** — 上传 PDF/DOCX/TXT 文档，自动解析分块、向量化存储，检索增强生成回答
- **业务办理（Agent Tools）** — 法规检索、空间查询、业务进度查询等工具调用
- **多会话管理** — 支持多轮对话、会话新建/切换、历史记录持久化
- **文档上传** — 支持 PDF、DOCX、TXT 格式，自动入库供 RAG 检索
- **多 LLM 提供商** — 支持智谱 GLM、DeepSeek、OpenAI，通过配置切换

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.5 + Java 17 + LangChain4j 1.13 |
| 前端 | Vue 3 + TypeScript + Vite 8 + Element Plus + Pinia |
| AI | LangChain4j（ChatModel、RAG、Tool、ChatMemory） |
| 向量模型 | All-MiniLM-L6-v2（ONNX 本地推理） |
| 文档解析 | Apache PDFBox + Apache POI |

## 项目结构

```
GeoSmart-Agent/
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/geosmart/agent/
│       ├── api/                      # REST 控制器（SSE 流式、文档上传）
│       ├── chat/                     # AI 对话装配与配置
│       ├── rag/                      # RAG 管道（文档摄入、向量化、检索）
│       ├── agent/tools/              # @Tool 工具组件
│       ├── llm/                      # 可插拔 LLM 提供商配置
│       └── config/                   # 应用配置与示例数据加载
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── views/ChatView.vue        # 聊天主界面
│       ├── stores/chat.ts            # Pinia 状态管理
│       └── api/chat.ts               # SSE 流式 API 客户端
└── README.md
```

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 20.19+ 或 22.12+
- LLM API Key（智谱 / DeepSeek / OpenAI 任选其一）

### 配置

编辑 `backend/src/main/resources/application.yml`，设置 LLM API Key：

```yaml
llm:
  provider: zhipu                    # 可选：zhipu | deepseek | openai
  zhipu:
    api-key: YOUR_ZHIPU_API_KEY
```

也可通过环境变量覆盖：`LLM_PROVIDER=zhipu`、`ZHIPU_API_KEY=xxx`。

### 启动

**1. 启动后端**

```bash
cd backend
mvn spring-boot:run                  # 端口 8080
```

**2. 启动前端**

```bash
cd frontend
npm install
npm run dev                          # 端口 5173，/api 代理至 localhost:8080
```

**3. 访问**

打开 http://localhost:5173

## 架构

```
Vue 3 (5173)  ──SSE/REST──▶  Spring Boot (8080)
                                  ├── GeoSmartAssistant (@SystemMessage + AiService)
                                  ├── ChatConfig (工具、RAG、会话装配)
                                  ├── Agent Tools (法规/空间/业务)
                                  ├── RAG Pipeline (解析→分块→向量化→检索)
                                  └── LLM Provider (智谱/DeepSeek/OpenAI)
```

**核心集成点：**
- `ChatConfig.java` — 装配 AiServices，注册工具、ContentRetriever、ChatMemoryProvider
- `GeoSmartAssistant.java` — 定义系统提示词和 `chat()` 接口契约
- `ChatView.vue` — 前端聊天界面，通过 `ReadableStream` 解析 SSE 流式数据

## 常用命令

```bash
# 后端
cd backend
mvn compile                          # 编译
mvn test                             # 运行全部测试
mvn test -Dtest=TestClassName        # 运行单个测试类
mvn spring-boot:run                  # 启动开发服务器

# 前端
cd frontend
npm run dev                          # 开发服务器 + HMR
npm run build                        # 类型检查 + 生产构建
npm run type-check                   # 仅 TypeScript 类型检查
npm run lint                         # oxlint + eslint
```

## License

Private — Internal Use Only
