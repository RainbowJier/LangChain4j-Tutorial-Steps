# LangChain4j 渐进式教学项目

从零到一构建 AI 应用 — 面向有经验的 Java 开发者。

通过 **10 个渐进式步骤**，掌握 LangChain4j 核心概念：LLM 调用、流式输出、RAG 检索增强生成、Agent 工具调用、会话记忆，最终构建完整的
Spring Boot + Vue 3 全栈应用。

## 学习路线

```
Step 00 ─── Step 01 ─── Step 02 ─── Step 03 ─── Step 04 ─── Step 05 ─── Step 06
环境准备    你好 LLM    流式输出    RAG 基础    Agent 工具   会话记忆    完整装配
  │                                                                 │
  └── 纯 Java（无需 Spring Boot）─────────────────────────────────────┘
                                                                        │
Step 07 ─── Step 08 ─── Step 09 ─── final/
Spring Boot  Vue 前端   生产优化     完整项目
  │
  └── Spring Boot + Vue 3 全栈 ──┘
```

## 各步骤概览

| 步骤                                        | 主题          | 学到什么                                | 运行方式                  |
|-------------------------------------------|-------------|-------------------------------------|-----------------------|
| [Step 00](steps/step-00-setup/)           | 环境准备        | 配置 API Key，验证连通性                    | `mvn exec:java`       |
| [Step 01](steps/step-01-hello-llm/)       | 你好 LLM      | ChatModel、AiServices、@SystemMessage | `mvn exec:java`       |
| [Step 02](steps/step-02-streaming/)       | 流式输出        | StreamingChatModel、TokenStream 回调   | `mvn exec:java`       |
| [Step 03](steps/step-03-rag-retrieval/)   | RAG 基础      | Embedding、向量存储、文档分块、检索              | `mvn exec:java`       |
| [Step 04](steps/step-04-agent-tools/)     | Agent 工具    | @Tool、@P、Function Calling           | `mvn exec:java`       |
| [Step 05](steps/step-05-memory-session/)  | 会话记忆        | ChatMemory、@MemoryId、多会话隔离          | `mvn exec:java`       |
| [Step 06](steps/step-06-full-aiservice/)  | 完整装配        | AiServices.builder() 综合集成           | `mvn exec:java`       |
| [Step 07](steps/step-07-spring-boot-api/) | Spring Boot | 多模块架构、@Bean、SSE 端点                  | `mvn spring-boot:run` |
| [Step 08](steps/step-08-vue-frontend/)    | Vue 前端      | Composition API、Pinia、SSE 客户端       | `npm run dev`         |
| [Step 09](steps/step-09-production/)      | 生产优化        | 向量数据库、监控、安全                         | `mvn exec:java`       |
| [final/](final/)                          | 完整项目        | 全栈参考实现                              | 全栈启动                  |

## 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 20.19+（仅 Step 08 和 final/）
- LLM API Key（推荐智谱 GLM，注册送免费额度）

### 30 分钟跑通核心流程

```bash
# 1. 克隆项目
git clone <repo-url>
cd langchain4j-tutorial

# 2. 配置 API Key
export LLM_API_KEY=your-key-here
# 或者编辑 steps/step-00-setup/src/main/resources/application.yml

# 3. 验证环境
cd steps/step-00-setup
mvn compile exec:java

# 4. 逐步学习
cd ../step-01-hello-llm && mvn compile exec:java
cd ../step-03-rag-retrieval && mvn compile exec:java   # 体验 RAG！
cd ../step-04-agent-tools && mvn compile exec:java     # 体验工具调用！
```

## 项目结构

```
langchain4j-tutorial/
├── steps/                           # 渐进式学习步骤（Step 00-09）
│   ├── step-00-setup/               # 环境准备
│   ├── step-01-hello-llm/           # 最简 LLM 调用
│   ├── step-02-streaming/           # 流式输出
│   ├── step-03-rag-retrieval/       # RAG 基础
│   ├── step-04-agent-tools/         # Agent 工具调用
│   ├── step-05-memory-session/      # 会话记忆
│   ├── step-06-full-aiservice/      # 完整 AiService 装配
│   ├── step-07-spring-boot-api/     # Spring Boot REST API
│   ├── step-08-vue-frontend/        # Vue 3 前端集成
│   └── step-09-production/          # 生产优化
├── final/                           # 完整可运行项目（最终形态）
│   ├── backend/                     # Spring Boot 多模块后端
│   └── frontend/                    # Vue 3 完整前端
├── docs/                            # 教学文档
│   ├── guides/                      # 每步学习指南
│   ├── diagrams/                    # 架构图
│   └── api-reference/               # API 速查
└── exercises/                       # 课后练习
    ├── solutions/                   # 参考答案
    └── challenges/                  # 挑战题
```

## 技术栈

| 层       | 技术                         | 版本        |
|---------|----------------------------|-----------|
| 后端      | Spring Boot + Java 17      | 3.5.0     |
| AI 框架   | LangChain4j                | 1.13.0    |
| 前端      | Vue 3 + TypeScript + Vite  | 3.5 / 8.0 |
| UI 组件   | Element Plus               | 2.13      |
| 状态管理    | Pinia                      | 3.0       |
| 向量模型    | All-MiniLM-L6-v2 (ONNX)    | 本地推理      |
| LLM 提供商 | 智谱 GLM / DeepSeek / OpenAI | 可切换       |

## 设计理念

1. **一步一概念**：每个步骤只引入一个新概念，其他保持不变
2. **独立可运行**：每个步骤都是完整可运行的 Maven 项目
3. **渐进式复杂度**：从 1 个 Java 文件 → 5 个 Maven 模块 → 全栈应用
4. **概念桥梁**：Step 06 的纯 Java 代码与 Step 07 的 Spring Boot 代码一一对照

## License

MIT
