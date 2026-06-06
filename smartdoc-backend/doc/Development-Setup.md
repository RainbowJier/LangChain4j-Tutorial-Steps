# SmartDoc 开发环境搭建

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2026-06-06 | 初始版本 |

---

## 环境要求

| 工具 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 使用 OpenJDK 或 Oracle JDK |
| Maven | 3.8+ | 项目构建工具 |
| IDE | — | IntelliJ IDEA 推荐 |

---

## 快速开始

### 1. 配置 API Key

```bash
# DeepSeek
set DEEPSEEK_API_KEY=sk-your-key-here
set LLM_PROVIDER=deepseek

# 或 智谱 GLM
set ZHIPU_API_KEY=your-key-here
set LLM_PROVIDER=zhipu

# 或 统一覆盖（优先级最高）
set LLM_API_KEY=sk-your-key-here
```

### 2. 构建所有模块

```bash
cd smartdoc-backend
mvn clean install -DskipTests
```

> 必须先执行此步骤，否则子模块依赖不可用。

### 3. 启动服务

```bash
cd smartdoc-api
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`。

> 注意：`mvn spring-boot:run` 必须在 `smartdoc-api` 目录下执行，不要在父目录执行。

---

## 验证服务可用

```bash
# 健康检查（无文档列表）
curl http://localhost:8080/api/documents

# 上传文档
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@test.pdf"

# 发起对话（SSE 流式）
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"message":"文档里写了什么？","sessionId":"test-001"}'
```

---

## 项目结构

```
smartdoc-backend/
├── pom.xml                          # 父 POM（模块聚合）
├── smartdoc-common/                 # 公共 DTO 和枚举
│   └── src/main/java/com/smartdoc/common/
├── smartdoc-chatModel/              # LLM + Embedding 配置
│   └── src/main/java/com/smartdoc/chatmodel/
├── smartdoc-rag/                    # RAG 检索服务
│   └── src/main/java/com/smartdoc/rag/
├── smartdoc-tools/                  # Agent 工具
│   └── src/main/java/com/smartdoc/agent/tools/
├── smartdoc-chat/                   # AiServices 装配 + 会话管理
│   └── src/main/java/com/smartdoc/chat/
├── smartdoc-api/                    # 启动入口 + REST 控制器
│   └── src/main/java/com/smartdoc/api/
│       ├── SmartDocApplication.java
│       ├── ChatController.java
│       ├── DocumentController.java
│       ├── AppConfig.java
│       ├── GlobalExceptionHandler.java
│       └── dto/
├── doc/                             # 开发文档
│   ├── PRD.md
│   ├── API-Reference.md
│   ├── Architecture.md
│   ├── SSE-Protocol.md
│   ├── Configuration.md
│   ├── Development-Setup.md
│   └── tasks.json
└── tasks.json                       # 开发任务跟踪
```

---

## 模块依赖

```bash
smartdoc-common ← smartdoc-chatModel ← smartdoc-rag ← smartdoc-tools
                                             ↓          ↓
                                        smartdoc-chat ←┘
                                              ↓
                                         smartdoc-api
```

构建时，Maven 按依赖顺序自动编译。如果修改了底层模块，需要重新 `mvn install` 上层模块才能感知变更。

---

## 常见问题

### 1. 启动报错 "Please configure API Key"

API Key 未配置或使用了占位符值。设置环境变量：

```bash
set DEEPSEEK_API_KEY=sk-real-key-not-placeholder
```

### 2. Module not found 编译错误

父 POM 未先安装。执行：

```bash
cd smartdoc-backend
mvn clean install -DskipTests
```

### 3. 上传文件报 413

文件超过 50MB。在 `application.yml` 中调整：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

### 4. SSE 连接 120 秒超时

这是服务端默认超时时间，前端需要在此时间内重连。超时时间在 `ChatController.java` 中配置：

```java
SseEmitter emitter = new SseEmitter(120_000L);
```

### 5. 文档解析后对话无结果

- 确保 `rag.embedding.type` 与模型文件匹配（本地 ONNX 需下载模型）
- 首次启动时本地模型会自动下载，可能需要几分钟
- 检查日志中的 `dev.langchain4j` DEBUG 输出

---

## IDE 配置建议

### IntelliJ IDEA

1. **启用注解处理**：`Settings → Build → Compiler → Annotation Processors → Enable annotation processing`
2. **安装 Lombok 插件**：`Settings → Plugins → Lombok`
3. **Run Configuration**：主类 `com.smartdoc.api.SmartDocApplication`，工作目录 `smartdoc-api/`
4. **VM Options**：不需要额外参数，所有配置通过环境变量或 `application.yml` 管理

### VS Code

安装扩展：
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support
