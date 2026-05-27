# Step 08 - Vue 3 前端

本步骤实现 SmartDoc 智能文档助手的前端界面，连接 Step 07 的 Spring Boot 后端。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架（Composition API + `<script setup>`）
- **Vite 8** - 新一代前端构建工具（开发服务器 + Rollup 生产构建）
- **TypeScript** - 类型安全的 JavaScript 超集
- **Pinia** - Vue 3 官方状态管理库
- **Element Plus** - Vue 3 UI 组件库
- **MarkdownIt** - Markdown 渲染引擎

## 项目结构

```
frontend/
├── index.html              # HTML 入口（挂载点 #app）
├── package.json            # 依赖与脚本
├── vite.config.ts          # Vite 配置（含开发代理）
├── tsconfig.json           # TypeScript 配置入口
├── tsconfig.app.json       # 应用代码 TS 配置
├── tsconfig.node.json      # 工具链 TS 配置
└── src/
    ├── main.ts             # 应用入口：创建 Vue 实例并注册插件
    ├── App.vue             # 根组件：渲染 <router-view>
    ├── router/index.ts     # 路由配置：URL 到组件的映射
    ├── types/index.ts      # TypeScript 类型定义
    ├── api/chat.ts         # 后端 API 封装（SSE 流式 + 文件上传）
    ├── stores/chat.ts      # Pinia 状态管理（会话、消息、流式状态）
    ├── views/ChatView.vue  # 聊天页面：布局容器
    ├── assets/main.css     # 全局样式
    └── components/
        ├── SessionList.vue  # 左侧边栏：会话列表
        ├── MessageList.vue  # 消息展示区：支持 Markdown 渲染
        ├── MessageInput.vue # 消息输入框：Enter 发送
        └── DocUpload.vue    # 文档上传：支持 PDF/DOCX/TXT
```

## 快速开始

```bash
# 1. 安装依赖
cd frontend
npm install

# 2. 启动开发服务器（需要后端已运行在 localhost:8080）
npm run dev

# 3. 打开浏览器访问 http://localhost:5173
```

## 核心概念

### SSE 流式通信

后端通过 Spring Boot 的 `SseEmitter` 逐 token 推送 AI 回复，前端使用 `fetch() + ReadableStream` 读取：

```
浏览器 fetch() → Spring Boot SseEmitter
                ← data:你
                ← data:好
                ← data:，
                ← data:世界
                ← data:[DONE]
```

### Vite 开发代理

开发模式下，Vite 将 `/api` 开头的请求代理到后端 `localhost:8080`，解决跨域问题：

```
浏览器 → localhost:5173/api/chat → Vite 代理 → localhost:8080/api/chat
```

### Pinia 状态管理

```
用户输入 → MessageInput 调用 store.sendMessage()
        → api/chat.ts 发起 SSE 请求
        → onToken 回调更新 store 中的消息内容
        → MessageList 响应式更新 UI
```

## 与 Step 07 后端的接口对应

| 前端 API 函数 | HTTP 方法 | 后端端点 | 功能 |
|---|---|---|---|
| `streamChat()` | POST | `/api/chat` | SSE 流式聊天 |
| `uploadDocument()` | POST | `/api/documents/upload` | 上传文档 |
| `listDocuments()` | GET | `/api/documents` | 获取文档列表 |

## 构建部署

```bash
npm run build    # TypeScript 类型检查 + 生产构建，输出到 dist/
npm run preview  # 本地预览生产构建结果
```
