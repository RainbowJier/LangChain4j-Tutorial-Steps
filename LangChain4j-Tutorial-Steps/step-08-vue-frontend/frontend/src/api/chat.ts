// ============================================================================
// api/chat.ts - 后端 API 调用封装
// ============================================================================
//
// 【教学要点】将所有与后端通信的逻辑集中在一个模块中，好处：
//   1. 组件和 Store 不需要关心 HTTP 请求的细节
//   2. 后端 API 变更时只需修改此文件
//   3. 便于添加统一的错误处理、认证头、日志等
//
// 【SSE 客户端实现原理】
//   后端使用 Spring Boot 的 SseEmitter 推送流式数据，格式为：
//     data:这是第一个token\n\n
//     data:这是第二个token\n\n
//     data:[DONE]\n\n
//
//   前端使用 fetch() + ReadableStream 读取，而不是 EventSource API，
//   因为 EventSource 只支持 GET 请求，而我们需要 POST 发送消息。
//
//   核心流程：
//   1. fetch() 发起 POST 请求，获取 Response 对象
//   2. response.body.getReader() 获取流式读取器
//   3. 循环调用 reader.read()，每次得到一个 Uint8Array 数据块
//   4. 用 TextDecoder 将字节解码为文本
//   5. 按行解析，提取 "data:" 前缀后面的内容
//   6. 通过回调函数将每个 token 传递给上层
//   7. 遇到 [DONE] 或流结束时调用 onComplete
//
//   AbortController 用于支持"停止生成"功能 —— 调用 controller.abort()
//   会中断正在进行的 fetch 请求。
// ============================================================================

import type { ChatRequest } from '@/types'

/**
 * 流式聊天接口 —— 通过 SSE (Server-Sent Events) 接收 AI 的逐 token 回复
 *
 * @param request   包含 message 和 sessionId 的请求体
 * @param onToken   每收到一个 token 时回调（用于逐字渲染）
 * @param onComplete 流式传输完成时回调
 * @param onError   发生错误时回调
 * @returns AbortController 可用于中断请求（实现"停止生成"功能）
 */
export function streamChat(
  request: ChatRequest,
  onToken: (token: string) => void,
  onComplete: () => void,
  onError: (error: Error) => void
): AbortController {
  // AbortController 是浏览器原生 API，可用于中断 fetch 请求
  const controller = new AbortController()

  // 注意：这里使用 /api/chat，Vite 开发服务器会将 /api 代理到 localhost:8080
  fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
    signal: controller.signal  // 绑定 AbortController，调用 abort() 会中断请求
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      // 获取 ReadableStream 的读取器 —— 这是流式读取的核心
      const reader = response.body!.getReader()
      const decoder = new TextDecoder()  // 将 Uint8Array 字节流解码为字符串
      let buffer = ''                     // 缓冲区：存储尚未完成解析的数据

      // 循环读取数据块，直到流结束
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        // 将新数据追加到缓冲区，然后按换行符分割
        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        // 最后一行可能是不完整的，保留在缓冲区等下次拼接
        buffer = lines.pop() || ''

        // 逐行解析 SSE 格式
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const data = line.substring(5)
            if (data.trim() === '[DONE]') {
              // 后端发送 [DONE] 表示流式传输结束
              onComplete()
            } else {
              // 每个非 [DONE] 的 data 都是一个 token 片段
              onToken(data)
            }
          }
        }
      }
      // 流正常结束（reader.read() 返回 done: true）
      onComplete()
    })
    .catch((err) => {
      // 用户主动中断（点击"停止"按钮）不算错误，静默忽略
      if (err.name !== 'AbortError') {
        onError(err)
      }
    })

  return controller
}

/**
 * 上传文档到后端知识库
 * 使用 FormData 发送 multipart/form-data 请求
 */
export async function uploadDocument(file: File): Promise<{ status: string; filename: string }> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/documents/upload', {
    method: 'POST',
    body: formData  // FormData 会自动设置 Content-Type: multipart/form-data
  })

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.statusText}`)
  }

  return response.json()
}

/**
 * 获取已上传文档列表
 */
export async function listDocuments(): Promise<{ documents: string[] }> {
  const response = await fetch('/api/documents')
  return response.json()
}
