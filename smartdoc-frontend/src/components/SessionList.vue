<template>
  <div class="session-panel">
    <div class="panel-head">
      <span class="panel-label">History</span>
      <button id="sidebar-new-btn" aria-label="New chat" class="new-btn" @click="onNew">
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
             width="14">
          <line x1="12" x2="12" y1="5" y2="19"/>
          <line x1="5" x2="19" y1="12" y2="12"/>
        </svg>
      </button>
    </div>
    <div class="list-scroll">
      <p v-if="chatStore.sessions.length === 0" class="empty-msg">No conversations yet</p>
      <TransitionGroup class="session-list-wrapper" name="s-fade" tag="div">
        <div
            v-for="session in chatStore.sessions" :id="`session-row-${session.id}`"
            :key="session.id"
            :class="['session-row', { active: session.id === chatStore.currentSessionId }]"
            role="button"
            tabindex="0" @click="onSelect(session.id)" @keydown.enter.space.prevent="onSelect(session.id)"
        >
          <div class="row-indicator"/>
          <span class="row-icon">
            <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                 viewBox="0 0 24 24" width="14">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </span>
          <span class="row-title">{{ session.title }}</span>
          <button aria-label="Delete" class="del-btn" tabindex="-1" @click.stop="chatStore.deleteSession(session.id)">
            <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                 width="13">
              <path d="M18 6L6 18"/>
              <path d="M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {useChatStore} from '@/stores/chat'

const emit = defineEmits<{ 'session-selected': [] }>()
const chatStore = useChatStore()
if (chatStore.sessions.length === 0) chatStore.createSession()

function onSelect(id: string) {
  chatStore.selectSession(id);
  emit('session-selected')
}

function onNew() {
  chatStore.createSession();
  emit('session-selected')
}
</script>

<style scoped>
.session-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-12) var(--spacing-16) var(--spacing-8);
}

.panel-label {
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-graphite);
}

.new-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-graphite);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-primary);
}

.new-btn:hover {
  background: var(--surface-canvas);
  color: var(--color-ink);
  transform: scale(1.08);
}

.new-btn:active {
  transform: scale(0.95);
}

.list-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 2px var(--spacing-12) var(--spacing-12);
}

.empty-msg {
  text-align: center;
  padding: 40px 16px;
  font-size: var(--text-body-sm);
  color: var(--color-graphite);
}

.session-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.session-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
  width: 100%;
  padding: var(--spacing-8) var(--spacing-8) var(--spacing-8) var(--spacing-12);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-graphite);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
  cursor: pointer;
  text-align: left;
  position: relative;
  transition: all var(--duration-fast) var(--ease-primary);
  white-space: nowrap;
}

.session-row:hover {
  background: var(--surface-canvas);
  color: var(--color-ink);
}

.session-row.active {
  background: transparent;
  color: var(--color-ink);
  font-weight: var(--weight-medium);
}

.row-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: var(--color-ink);
  border-radius: 0 2px 2px 0;
  transition: height var(--duration-primary) var(--ease-primary);
}

.session-row.active .row-indicator {
  height: 16px;
}

.row-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  opacity: 0.3;
  transition: all var(--duration-fast) var(--ease-primary);
}

.session-row.active .row-icon {
  opacity: 0.5;
}

.session-row:hover .row-icon {
  opacity: 0.4;
}

.row-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.del-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-graphite);
  cursor: pointer;
  opacity: 0;
  pointer-events: none;
  transition: all var(--duration-fast) var(--ease-primary);
}

.session-row:hover .del-btn {
  opacity: 1;
  pointer-events: auto;
}

.del-btn:hover {
  background: rgba(255, 59, 48, 0.08);
  color: #ff3b30;
}

.s-fade-enter-active, .s-fade-leave-active {
  transition: all 0.25s var(--ease-primary);
}

.s-fade-enter-from {
  opacity: 0;
  transform: translateY(-4px);
}

.s-fade-leave-to {
  opacity: 0;
  transform: translateX(6px);
}

.s-fade-move {
  transition: transform 0.25s var(--ease-primary);
}
</style>
