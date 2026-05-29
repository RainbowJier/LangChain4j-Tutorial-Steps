<template>
  <div class="session-list">
    <div class="session-header">
      <span>History</span>
      <el-button type="primary" size="small" @click="chatStore.createSession()">
        + New
      </el-button>
    </div>

    <div class="session-items">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        :class="['session-item', { active: session.id === chatStore.currentSessionId }]"
      >
        <div class="session-info" @click="chatStore.selectSession(session.id)">
          <span class="session-title">{{ session.title }}</span>
        </div>
        <el-button
          class="session-delete"
          size="small"
          type="danger"
          link
          @click.stop="chatStore.deleteSession(session.id)"
        >
          ×
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

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
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  margin: 2px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.session-info {
  flex: 1;
  cursor: pointer;
  overflow: hidden;
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
  display: block;
}

.session-delete {
  opacity: 0;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.session-item:hover .session-delete {
  opacity: 1;
}
</style>
