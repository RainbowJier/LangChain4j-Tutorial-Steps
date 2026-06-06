# SmartDoc 配置参考

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2026-06-06 | 初始版本 |

---

## 环境变量

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `LLM_API_KEY` | 否 | — | 覆盖所有供应商的 API Key |
| `LLM_PROVIDER` | 否 | `zhipu` | 当前使用的 LLM 供应商 (`zhipu` / `deepseek`) |
| `DEEPSEEK_API_KEY` | 条件必填 | — | DeepSeek API Key（使用 DeepSeek 时需设置） |
| `ZHIPU_API_KEY` | 条件必填 | — | 智谱 API Key（使用智谱时需设置） |
| `RAG_EMBEDDING_TYPE` | 否 | `local` | Embedding 类型 (`local` / `zhipu`) |

启动时会对 API Key 做占位符校验，如果检测到 `your-*-api-key-here` 模式会抛出异常。

---

## application.yml 全配置

```yaml
server:
  port: 8080                              # 服务端口

spring:
  servlet:
    multipart:
      max-file-size: 50MB                  # 单文件上传限制
      max-request-size: 50MB               # 单次请求大小限制

llm:
  provider: ${LLM_PROVIDER:zhipu}          # 当前供应商
  providers:
    zhipu:
      base-url: https://open.bigmodel.cn/api/coding/paas/v4
      api-key: ${ZHIPU_API_KEY:your-zhipu-api-key-here}
      model-name: glm-5.1
      max-tokens: 8192
    deepseek:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY:your-deepseek-api-key-here}
      model-name: deepseek-v4-flash
      max-tokens: 8192

rag:
  embedding:
    type: ${RAG_EMBEDDING_TYPE:local}      # local 或 zhipu
  chunk-size: 1000                          # 文档分块大小（字符数）
  chunk-overlap: 100                        # 分块重叠大小
  max-results: 5                            # RAG 检索最大返回片段数
  document-registry: data/document-registry.txt  # 文档注册表文件路径

chat:
  max-memory-messages: 20                   # 每会话最大消息数
  session-timeout-minutes: 30               # 会话超时时间（分钟）
  eviction-interval-ms: 300000              # 过期会话清理间隔（毫秒）

logging:
  level:
    dev.langchain4j: DEBUG                  # LangChain4j 日志级别
    com.smartdoc: DEBUG                     # 应用日志级别
```

---

## 配置项说明

### LLM 配置

| 路径 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `llm.provider` | String | `zhipu` | 激活的供应商 key，对应 `llm.providers` 下的子项 |
| `llm.providers.*.base-url` | String | — | OpenAI 兼容 API 的 base URL |
| `llm.providers.*.api-key` | String | — | API Key，支持环境变量注入 |
| `llm.providers.*.model-name` | String | — | 模型名称 |
| `llm.providers.*.max-tokens` | Integer | 8192 | 最大输出 Token 数 |

### RAG 配置

| 路径 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `rag.embedding.type` | String | `local` | `local` = AllMiniLmL6V2（ONNX 本地），`zhipu` = 远程 API |
| `rag.chunk-size` | Integer | 1000 | 文档分块大小（字符） |
| `rag.chunk-overlap` | Integer | 100 | 分块重叠字符数 |
| `rag.max-results` | Integer | 5 | RAG 检索返回的 Top-K 片段数 |
| `rag.document-registry` | String | `data/document-registry.txt` | 文档注册表文件路径 |

### 会话配置

| 路径 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `chat.max-memory-messages` | Integer | 20 | 每会话保留最近 N 条消息，超出则丢弃最旧消息 |
| `chat.session-timeout-minutes` | Integer | 30 | 会话超过此时间未活跃自动清理 |
| `chat.eviction-interval-ms` | Long | 300000 | 过期会话扫描间隔（毫秒） |

### 文件上传

| 路径 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `spring.servlet.multipart.max-file-size` | String | `50MB` | 单文件大小上限 |
| `spring.servlet.multipart.max-request-size` | String | `50MB` | 单次请求大小上限 |

---

## 添加新的 LLM 供应商

在 `application.yml` 的 `llm.providers` 下添加新条目，任何兼容 OpenAI API 的服务均可：

```yaml
llm:
  providers:
    qwen:                                    # 供应商名称
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${QWEN_API_KEY:your-key-here}
      model-name: qwen-turbo
      max-tokens: 8192
```

然后设置环境变量 `LLM_PROVIDER=qwen` 切换。

---

## 切换 Embedding 模型

### 本地 ONNX 模式（默认，英文优化）

```
set RAG_EMBEDDING_TYPE=local
```

使用 `AllMiniLmL6V2EmbeddingModel`，本地 ONNX Runtime 推理。

### 远程智谱模式（中文优化）

```
set RAG_EMBEDDING_TYPE=zhipu
```

使用 `ZhipuAiEmbeddingModel`，调用远程 API。需要 `ZHIPU_API_KEY` 环境变量。
