// ============================================================================
// stores/chat.ts - Pinia 聊天状态管理
// ============================================================================
//
// 【教学要点】Pinia 是 Vue 3 官方推荐的状态管理库（替代 Vuex），核心概念：
//
//   1. Store = 全局响应式数据仓库，任何组件都可以读写
//   2. defineStore() 定义一个 store，返回 useXxxStore() 组合式函数
//   3. 两种定义风格：
//      - Options API 风格（类似 Vuex）：{ state, getters, actions }
//      - Composition API 风格（本文件使用）：更灵活，类似 Vue 组件的 setup()
//
// 【Composition API 风格的 Store 结构】
//   ref()   -> 等同于 state（响应式数据）
//   computed() -> 等同于 getters（派生数据）
//   function() -> 等同于 actions（修改状态的方法）
//   return { ... } -> 暴露给组件使用的公共接口
//
// 【本 Store 管理的数据】
//   - sessions: 所有聊天会话列表
//   - currentSessionId: 当前选中的会话 ID
//   - messages: 每个会话的消息记录（Map<sessionId, ChatMessage[]>）
//   - isLoading: 是否正在等待 AI 回复
//
// 【数据流向】
//   用户输入 -> sendMessage() -> streamChat() API -> onToken 回调
//   -> 更新 messages Map -> Vue 响应式系统 -> MessageList 重新渲染
// ============================================================================

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatMessage, ChatSession } from '@/types'
import { streamChat } from '@/api/chat'

/** 生成唯一 ID：时间戳 + 随机字符串 */
function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

export const useChatStore = defineStore('chat', () => {
  // ---- State（响应式状态）----

  /** 所有聊天会话列表 */
  const sessions = ref<ChatSession[]>([])

  /** 当前选中的会话 ID */
  const currentSessionId = ref<string>('')

  /**
   * 消息映射表：sessionId -> 该会话的消息数组
   * 使用 Map 而非 Object 是因为 Map 的 key 可以是任意字符串，且性能更好
   */
  const messages = ref<Map<string, ChatMessage[]>>(new Map())

  /** 是否正在流式接收 AI 回复（用于控制 UI 状态，如禁用输入框） */
  const isLoading = ref(false)

  /** AbortController 引用 —— 用于中断正在进行的流式请求 */
  let abortController: AbortController | null = null

  // ---- Getters（派生数据）----

  /** 当前会话的消息列表 —— 自动跟随 currentSessionId 变化 */
  const currentMessages = computed(() => {
    return messages.value.get(currentSessionId.value) || []
  })

  // ---- Actions（状态修改方法）----

  /**
   * 创建新的聊天会话
   * @returns 新创建的会话 ID
   */
  function createSession(): string {
    const id = generateId()
    const session: ChatSession = {
      id,
      title: '新对话',
      createdAt: Date.now(),
      lastMessageAt: Date.now()
    }
    // unshift 插入到数组头部，最新的会话显示在最上面
    sessions.value.unshift(session)
    messages.value.set(id, [])
    currentSessionId.value = id
    return id
  }

  /**
   * 切换到指定会话
   */
  function selectSession(id: string) {
    currentSessionId.value = id
  }

  /**
   * 发送用户消息并接收 AI 流式回复
   *
   * 流程：
   * 1. 确保有活跃会话（没有则自动创建）
   * 2. 添加用户消息到消息列表
   * 3. 添加一个空的 AI 消息占位（显示加载状态）
   * 4. 调用 streamChat API，通过回调逐步填充 AI 消息内容
   */
  function sendMessage(content: string) {
    // 如果没有当前会话，自动创建一个
    if (!currentSessionId.value) {
      createSession()
    }

    const sessionId = currentSessionId.value

    // 1. 创建并添加用户消息
    const userMsg: ChatMessage = {
      id: generateId(),
      role: 'user',
      content,
      timestamp: Date.now()
    }
    const current = messages.value.get(sessionId) || []
    messages.value.set(sessionId, [...current, userMsg])

    // 2. 用第一条用户消息的前 20 个字符作为会话标题
    const session = sessions.value.find((s) => s.id === sessionId)
    if (session && current.length === 0) {
      session.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
    }

    // 3. 添加 AI 消息占位符（内容为空，标记为流式中）
    const assistantMsg: ChatMessage = {
      id: generateId(),
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
      isStreaming: true
    }
    messages.value.set(sessionId, [...(messages.value.get(sessionId) || []), assistantMsg])
    isLoading.value = true

    // 4. 发起流式请求
    abortController = streamChat(
      { message: content, sessionId },

      // onToken：每收到一个 token，追加到 AI 消息的内容中
      (token) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content += token
          // 创建新数组触发 Vue 的响应式更新
          // （直接修改数组元素的属性可能不会触发更新，替换整个数组确保响应式）
          messages.value.set(sessionId, [...msgs])
        }
      },

      // onComplete：流式传输完成
      () => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      },

      // onError：发生错误
      (error) => {
        const msgs = messages.value.get(sessionId) || []
        const last = msgs[msgs.length - 1]
        if (last && last.role === 'assistant') {
          last.content = `错误: ${error.message}`
          last.isStreaming = false
          messages.value.set(sessionId, [...msgs])
        }
        isLoading.value = false
      }
    )
  }

  /**
   * 停止当前流式传输（用户点击"停止"按钮时调用）
   */
  function stopStreaming() {
    abortController?.abort()
    isLoading.value = false
  }

  // 暴露公共接口 —— 组件通过 useChatStore() 解构使用
  return {
    sessions,
    currentSessionId,
    messages,
    isLoading,
    currentMessages,
    createSession,
    selectSession,
    sendMessage,
    stopStreaming
  }
})
