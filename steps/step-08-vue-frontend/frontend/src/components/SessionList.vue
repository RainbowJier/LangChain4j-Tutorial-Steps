<!-- ============================================================================
     SessionList.vue - 会话列表组件（左侧边栏）
     ============================================================================
     【教学要点】
       1. 展示所有聊天会话，支持切换和新建
       2. 通过 useChatStore() 直接读写全局状态，无需 props/events
       3. 使用 v-for 列表渲染，:class 动态绑定样式
     ============================================================================ -->

<template>
  <div class="session-list">
    <!-- 顶部标题栏 + 新建按钮 -->
    <div class="session-header">
      <span>历史对话</span>
      <el-button type="primary" size="small" @click="chatStore.createSession()">
        新建
      </el-button>
    </div>

    <!-- 会话列表 -->
    <div class="session-items">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        :class="['session-item', { active: session.id === chatStore.currentSessionId }]"
        @click="chatStore.selectSession(session.id)"
      >
        <span class="session-title">{{ session.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'

// 获取 Store 实例 —— 组件内直接使用，自动追踪依赖关系
const chatStore = useChatStore()

// 初始化：如果没有任何会话，自动创建一个
if (chatStore.sessions.length === 0) {
  chatStore.createSession()
}
</script>

<style scoped>
.session-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  font-weight: 600;
  color: #303133;
}

.session-items {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  padding: 12px 16px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #e4e7ed;
}

.session-item.active {
  background-color: #409eff;
  color: #fff;
}

.session-title {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
