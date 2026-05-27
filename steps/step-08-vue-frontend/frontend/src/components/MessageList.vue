<!-- ============================================================================
     MessageList.vue - 消息列表组件
     ============================================================================
     【教学要点】
       1. 展示当前会话的所有消息（用户消息 + AI 回复）
       2. AI 回复使用 MarkdownIt 渲染 Markdown 格式
       3. 流式输出时显示闪烁光标动画
       4. 使用 watch 监听消息变化，自动滚动到底部
       5. v-html 指令用于渲染 Markdown 转换后的 HTML（注意 XSS 安全）

     【Markdown 渲染流程】
       AI 回复文本 -> MarkdownIt.render() -> HTML 字符串 -> v-html 显示
       注意：html: false 配置禁止 Markdown 中的原生 HTML 标签，防止 XSS
     ============================================================================ -->

<template>
  <div class="message-list" ref="listRef">
    <!-- 空状态：没有消息时显示欢迎词 -->
    <div v-if="chatStore.currentMessages.length === 0" class="empty-state">
      <p>欢迎使用 SmartDoc 智能文档助手</p>
      <p class="hint">上传文档后，您可以提问关于文档内容的任何问题</p>
    </div>

    <!-- 消息列表 -->
    <div
      v-for="msg in chatStore.currentMessages"
      :key="msg.id"
      :class="['message', msg.role]"
    >
      <!-- 头像 -->
      <div class="message-avatar">
        {{ msg.role === 'user' ? '我' : 'AI' }}
      </div>

      <!-- 消息内容 -->
      <div class="message-content">
        <!-- AI 消息：渲染 Markdown -->
        <div v-if="msg.role === 'assistant'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
        <!-- 用户消息：纯文本 -->
        <div v-else>{{ msg.content }}</div>
        <!-- 流式输出时的闪烁光标 -->
        <span v-if="msg.isStreaming" class="streaming-cursor">|</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import MarkdownIt from 'markdown-it'

const chatStore = useChatStore()

// DOM 引用：用于操控消息列表的滚动位置
const listRef = ref<HTMLElement>()

// 初始化 Markdown 渲染器
const md = new MarkdownIt({
  html: false,    // 禁止渲染 HTML 标签（安全考虑）
  linkify: true,  // 自动识别 URL 并转为链接
  breaks: true    // 换行符转为 <br>
})

/** 将 Markdown 文本渲染为 HTML */
function renderMarkdown(content: string): string {
  return md.render(content)
}

/** 滚动到消息列表底部 —— 确保最新消息始终可见 */
function scrollToBottom() {
  // nextTick 等待 DOM 更新完成后再滚动
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

// 监听消息数量变化（新消息到来时滚动）
watch(() => chatStore.currentMessages.length, scrollToBottom)

// 监听最后一条消息的内容变化（流式输出 token 时持续滚动）
watch(
  () => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content,
  scrollToBottom
)
</script>

<style scoped>
.message-list {
  height: 100%;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding-top: 120px;
  color: #909399;
}

.empty-state .hint {
  margin-top: 8px;
  font-size: 14px;
  color: #c0c4cc;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background-color: #409eff;
  color: #fff;
}

.message.assistant .message-avatar {
  background-color: #f0f2f5;
  color: #606266;
}

.message-content {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-content {
  background-color: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.message.assistant .message-content {
  background-color: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
}

/* 流式输出时的闪烁光标动画 */
.streaming-cursor {
  animation: blink 1s infinite;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

/* Markdown 内容的样式覆盖（:deep 穿透 scoped 限制） */
.markdown-body :deep(pre) {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-body :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}
</style>
