<!-- ============================================================================
     MessageInput.vue - 消息输入组件
     ============================================================================
     【教学要点】
       1. 使用 Element Plus 的 el-input textarea 和 el-button 组件
       2. v-model 双向绑定实现输入框内容与 JS 变量同步
       3. @keydown 监听键盘事件，实现 Enter 发送、Shift+Enter 换行
       4. isLoading 状态控制按钮文案（发送/停止）和输入框禁用
     ============================================================================ -->

<template>
  <div class="message-input">
    <!-- 文本输入框 -->
    <el-input
      v-model="inputText"
      type="textarea"
      :rows="2"
      placeholder="输入消息... (Enter 发送, Shift+Enter 换行)"
      resize="none"
      @keydown="handleKeydown"
      :disabled="chatStore.isLoading"
    />
    <!-- 发送/停止按钮 -->
    <el-button
      type="primary"
      @click="handleSend"
      :disabled="!inputText.trim() && !chatStore.isLoading"
    >
      {{ chatStore.isLoading ? '停止' : '发送' }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const inputText = ref('')

/** 键盘事件处理：Enter 发送，Shift+Enter 换行 */
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()  // 阻止默认的换行行为
    handleSend()
  }
}

/** 发送或停止 */
function handleSend() {
  // 如果正在流式输出，点击按钮意味着"停止"
  if (chatStore.isLoading) {
    chatStore.stopStreaming()
    return
  }

  const text = inputText.value.trim()
  if (!text) return

  chatStore.sendMessage(text)
  inputText.value = ''  // 发送后清空输入框
}
</script>

<style scoped>
.message-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  margin-top: 8px;
}

.message-input .el-textarea {
  flex: 1;
}
</style>
