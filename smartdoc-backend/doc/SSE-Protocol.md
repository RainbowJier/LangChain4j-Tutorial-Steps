# SmartDoc SSE 协议文档

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0 | 2026-06-06 | 初始版本 |

---

## 概述

SmartDoc 使用 **Server-Sent Events (SSE)** 实现 AI 回复的流式输出。前端通过 `fetch` POST 请求建立连接，服务端以 `text/event-stream` 格式逐 token 推送。

> 注意：由于请求体需要传递 `message` 和 `sessionId`，**不能使用标准 `EventSource` API**（它只支持 GET 请求）。需要使用 `fetch` + `ReadableStream` 或 SSE polyfill。

---

## 连接建立

```
POST /api/chat
Content-Type: application/json
Accept: text/event-stream

{"message": "你好", "sessionId": "abc-123"}
```

服务端返回 `Content-Type: text/event-stream`，开始 SSE 推送。

---

## 事件格式

### 1. Token 事件（正常回复）

每收到一个 LLM 生成的 token，推送一个事件：

```
data:Spring
data: Boot
data: 是
data: 一
data: 个
```

前端将每个 `data:` 内容拼接起来得到完整回复。

### 2. 完成事件

所有 token 推送完毕后，发送结束标记：

```
data:[DONE]
```

前端收到此事件后应关闭连接。

### 3. 错误事件

发生错误时，推送 JSON 格式错误事件后关闭连接：

```
data:{"error":"LLM service is temporarily unavailable"}
```

### 4. 连接超时

SSE 连接最长保持 120 秒。超时后服务端自动断开，前端需重新发起请求。

---

## 前端实现示例

### 使用 fetch + ReadableStream

```javascript
async function sendChatMessage(message, sessionId) {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({ message, sessionId })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let reply = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const text = decoder.decode(value);
    const lines = text.split('\n');

    for (const line of lines) {
      if (!line.startsWith('data:')) continue;

      const data = line.slice(5).trim();

      if (data === '[DONE]') {
        console.log('回复完成:', reply);
        return reply;
      }

      if (data.startsWith('{')) {
        try {
          const err = JSON.parse(data);
          console.error('错误:', err.error);
          throw new Error(err.error);
        } catch (e) {
          if (e instanceof SyntaxError) continue;
          throw e;
        }
      }

      reply += data;
    }
  }

  return reply;
}
```

### 使用 EventSource polyfill

```javascript
// 需要安装事件流 polyfill
import { EventSourcePolyfill } from 'event-source-polyfill';

function createChatStream(message, sessionId, onToken, onError, onDone) {
  return fetch('/api/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify({ message, sessionId })
  }).then(response => {
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    function process() {
      reader.read().then(({ done, value }) => {
        if (done) { onDone(); return; }

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed.startsWith('data:')) continue;

          const data = trimmed.slice(5).trim();
          if (data === '[DONE]') { onDone(); return; }

          if (data.startsWith('{')) {
            try {
              const err = JSON.parse(data);
              onError(err.error);
              return;
            } catch { /* ignore parse errors on partial lines */ }
          }

          onToken(data);
        }

        process();
      }).catch(onError);
    }

    process();
  });
}
```

---

## 状态码与错误场景

| 场景 | HTTP 状态码 | 表现 |
|------|------------|------|
| 成功流式 | 200 | 逐 token 推送，`[DONE]` 结束 |
| message 为空 | 400 | 非 SSE，返回 JSON |
| message 超长 | 400 | 非 SSE，返回 JSON |
| 会话并发冲突 | 503 | SSE 返回 `{"error":"Current session is busy..."}` |
| LLM 服务异常 | 200 | SSE 返回 `{"error":"LLM service is temporarily unavailable"}` |
| SSE 连接超时 | — | 连接断开，前端 `reader.read()` 返回 `done: true` |
| 客户端断开 | — | `onPartialResponse` 捕获 IOException，服务端取消 LLM 调用 |

---

## 最佳实践

1. **超时重试**：前端应处理 120s 超时，自动重新发起请求
2. **会话复用**：断连后使用相同 sessionId 重连，历史消息保留
3. **错误恢复**：收到 error 事件后展示提示，不自动重试（避免死循环）
4. **URL 编码**：message 中的特殊字符由 JSON 序列化处理，无需额外编码
