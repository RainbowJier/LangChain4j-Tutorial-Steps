# SmartDoc API 参考文档

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2026-06-06 | 初始版本 |

---

## 全局约定

### Base URL

```
http://localhost:8080
```

### 全局响应格式

所有非 SSE 接口统一返回 `AjaxResult` 结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

### HTTP 状态码 vs code 字段

| HTTP 状态码 | code | 含义 |
|------------|------|------|
| 200 | 200 | 成功 |
| 400 | 400 | 参数校验失败 / 请求错误 |
| 413 | 413 | 文件超过 50MB 限制 |
| 500 | 500 | 服务器内部错误 |

### 枚举值

| code | 含义 |
|------|------|
| 200 | Operation successful |
| 400 | Bad request |
| 401 | Not logged in or token expired |
| 403 | Access denied |
| 404 | Resource not found |
| 409 | Resource conflict |
| 417 | Parameter validation failed |
| 429 | Too many requests |
| 500 | Operation failed |
| 503 | Service unavailable |
| 504 | Gateway timeout |

---

## 1. 智能对话（SSE 流式）

### POST /api/chat

流式返回 AI 回复，每 token 以 SSE 事件推送。

#### Request

```
POST /api/chat
Content-Type: application/json
Accept: text/event-stream
```

```json
{
  "message": "Spring Boot 如何配置拦截器？",
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| message | String | 是 | — | 用户问题，1-2000 字符，不能为空 |
| sessionId | String | 否 | `"default"` | 会话标识，UUID 格式，为空时使用默认值 |

#### Response

SSE 流式响应，`Content-Type: text/event-stream`

```
data:<token 文本>
data:<token 文本>
data:{"error":"<错误信息>"}
data:[DONE]
```

- 正常：一条 `data:` 一个 token，以 `data:[DONE]` 结束
- 错误：一条 `data:{"error":"..."}` 后关闭连接

#### Error 事件

| 错误场景 | error 消息 |
|---------|-----------|
| 会话并发冲突 | `Current session is busy, please try again later` |
| LLM 服务不可用 | `LLM service is temporarily unavailable` |
| SSE 连接中断 | `SSE connection lost` |
| 启动流失败 | `Failed to start chat stream` |

#### 前端 SSE 处理示例

```javascript
const es = new EventSourcePolyfill('/api/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message: '你好', sessionId: 'abc' })
});

let reply = '';
es.onmessage = (event) => {
  if (event.data === '[DONE]') {
    es.close();
    return;
  }
  if (event.data.startsWith('{')) {
    const err = JSON.parse(event.data);
    console.error('Chat error:', err.error);
    es.close();
    return;
  }
  reply += event.data;
};
```

#### 校验规则

| 规则 | 说明 |
|------|------|
| message 不能为空 | `@NotBlank` |
| message 长度 | 1-2000 字符，`@Size(max=2000)` |
| 会话并发 | 同一 sessionId 同时只能有一个活跃请求，并发返回 503 |

---

## 2. 对话历史

### GET /api/chat/history/{sessionId}

#### Request

```
GET /api/chat/history/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | String (path) | 是 | 会话标识 |

#### Response

```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "role": "user", "content": "Spring Boot 如何配置拦截器？" },
    { "role": "assistant", "content": "在 Spring Boot 中配置拦截器需要实现 HandlerInterceptor 接口..." }
  ]
}
```

- 只返回 `user` 和 `assistant` 角色，过滤系统提示
- 会话不存在或已过期 → 返回空数组 `[]`

---

## 3. 清除会话

### DELETE /api/chat/session/{sessionId}

#### Request

```
DELETE /api/chat/session/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

#### Response

```json
{
  "code": 200,
  "msg": "success",
  "data": null
}
```

- 幂等操作：会话不存在也返回成功
- 清除后该 sessionId 可重新使用

---

## 4. 上传文档

### POST /api/documents/upload

#### Request

```
POST /api/documents/upload
Content-Type: multipart/form-data
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 文档文件 |

#### Response (成功)

```json
{
  "code": 200,
  "msg": "success",
  "data": "Spring Boot Guide.pdf"
}
```

#### Response (格式不支持)

HTTP 400

```json
{
  "code": 400,
  "msg": "Unsupported file format: .exe. Supported: PDF, DOCX, DOC, TXT",
  "data": null
}
```

#### Response (文件过大)

HTTP 413

```json
{
  "code": 413,
  "msg": "File size exceeds the maximum allowed limit (50 MB)",
  "data": null
}
```

#### Response (解析失败)

HTTP 500

```json
{
  "code": 500,
  "msg": "Failed to process document. Please check the file format and try again.",
  "data": null
}
```

#### 约束

| 约束 | 值 |
|------|------|
| 支持格式 | PDF, DOCX, DOC, TXT |
| 最大文件大小 | 50MB |
| 解析方式 | PDF → Apache PDFBox, DOCX → Apache POI, TXT → 文本解析 |
| 分块策略 | 1000 字符/块，重叠 100 字符（可配置） |
| 向量存储 | `InMemoryEmbeddingStore`（重启丢失） |
| 文件名持久化 | `data/document-registry.txt`（启动时加载） |
| 临时文件 | 解析完成后自动清理 (`data/temp/`) |

---

## 5. 文档列表

### GET /api/documents

#### Request

```
GET /api/documents
```

#### Response

```json
{
  "code": 200,
  "msg": "success",
  "data": ["手册.pdf", "规范.docx"]
}
```

- 无文档时返回空数组 `{"code":200,"data":[]}`

---

## 接口汇总

| 方法 | 路径 | 说明 | 请求体 | 响应 | 优先级 |
|------|------|------|--------|------|--------|
| POST | `/api/chat` | SSE 流式对话 | `{message, sessionId}` | `text/event-stream` | P0 |
| GET | `/api/chat/history/{sessionId}` | 对话历史 | — | `AjaxResult<ChatHistoryItem[]>` | P1 |
| DELETE | `/api/chat/session/{sessionId}` | 清除会话 | — | `AjaxResult` | P2 |
| POST | `/api/documents/upload` | 上传文档 | `multipart/file` | `AjaxResult<String>` | P0 |
| GET | `/api/documents` | 文档列表 | — | `AjaxResult<String[]>` | P1 |
