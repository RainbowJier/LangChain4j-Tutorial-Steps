<template>
  <div class="app-shell">

    <aside :class="{ open: isSidebarOpen }" class="sidebar">
      <div class="sidebar-head">
        <div class="sidebar-brand">
          <div class="sidebar-icon">
            <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <span class="sidebar-title">SmartDoc</span>
        </div>
        <button aria-label="Close sidebar" class="sidebar-close-btn" @click="isSidebarOpen = false">
          <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
               width="14">
            <line x1="18" x2="6" y1="6" y2="18"/>
            <line x1="6" x2="18" y1="6" y2="18"/>
          </svg>
        </button>
      </div>
      <div class="sidebar-divider"/>
      <div class="sidebar-body">
        <SessionList @session-selected="isSidebarOpen = false"/>
      </div>
    </aside>

    <div v-if="isSidebarOpen" class="sidebar-overlay" @click="isSidebarOpen = false"/>

    <div class="main-col">
      <header class="topbar">
        <button aria-label="Toggle Navigation" class="sidebar-toggle" @click="isSidebarOpen = !isSidebarOpen">
          <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
               width="14">
            <line x1="4" x2="20" y1="6" y2="6"/>
            <line x1="4" x2="20" y1="12" y2="12"/>
            <line x1="4" x2="20" y1="18" y2="18"/>
          </svg>
        </button>
        <div class="topbar-brand">
          <div class="topbar-dot"/>
          <span class="topbar-label">SmartDoc Assistant</span>
        </div>
        <div class="topbar-spacer"/>
        <button class="topbar-cta" @click="createNewChat">New Chat</button>
      </header>

      <main class="chat-main">
        <div class="chat-scroll">
          <MessageList/>
        </div>
        <div class="chat-input-area">
          <MessageInput/>
        </div>
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import SessionList from '@/components/SessionList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import {useChatStore} from '@/stores/chat'

const isSidebarOpen = ref(false)
const chatStore = useChatStore()

function createNewChat() {
  chatStore.createSession();
  isSidebarOpen.value = false
}
</script>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--surface-canvas);
}

/* ── Sidebar ──────────────────────────────────────────── */
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--surface-card);
  border-right: 1px solid var(--color-silver-mist);
  z-index: 11;
  position: relative;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-20);
  height: var(--nav-height);
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
}

.sidebar-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  background: var(--color-ink);
  color: var(--color-snow);
}

.sidebar-title {
  font-family: var(--font-text);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-body-sm);
  color: var(--color-ink);
}

.sidebar-close-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-graphite);
  cursor: pointer;
  transition: background var(--duration-fast) var(--ease-primary);
}

.sidebar-close-btn:hover {
  background: var(--surface-canvas);
}

.sidebar-divider {
  height: 1px;
  background: var(--color-silver-mist);
  margin: 0 var(--spacing-20);
}

.sidebar-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ── Main Column ──────────────────────────────────────── */
.main-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

/* ── Top Bar ──────────────────────────────────────────── */
.topbar {
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-24);
  height: var(--nav-height);
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-silver-mist);
  background: rgba(250, 250, 252, 0.92);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  z-index: 10;
  position: relative;
}

.sidebar-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--color-silver-mist);
  border-radius: var(--radius-sm);
  background: var(--surface-card);
  color: var(--color-ink);
  cursor: pointer;
  margin-right: var(--spacing-12);
  transition: all var(--duration-fast) var(--ease-primary);
}

.sidebar-toggle:hover {
  background: var(--surface-canvas);
}

.topbar-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
}

.topbar-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #30d158;
  flex-shrink: 0;
}

.topbar-label {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--color-ink);
  letter-spacing: var(--tracking-body-sm);
}

.topbar-spacer {
  flex: 1;
}

.topbar-cta {
  font-family: var(--font-text);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--color-azure);
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--spacing-8) var(--spacing-12);
  border-radius: var(--radius-sm);
  transition: background var(--duration-fast) var(--ease-primary);
}

.topbar-cta:hover {
  background: rgba(0, 113, 227, 0.08);
}

/* ── Overlay ──────────────────────────────────────────── */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(2px);
  z-index: 100;
  animation: fadeIn 0.15s ease-out;
}

/* ── Chat Main ────────────────────────────────────────── */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
  background: var(--surface-canvas);
  position: relative;
}

.chat-scroll {
  flex: 1;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.chat-input-area {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  padding: var(--spacing-12) var(--spacing-24) var(--spacing-20);
  position: relative;
  z-index: 1;
  background: linear-gradient(to top, var(--surface-canvas) 70%, transparent);
}

/* ── Responsive ────────────────────────────────────────── */
@media (max-width: 768px) {
  .sidebar-toggle {
    display: flex;
  }

  .sidebar-close-btn {
    display: flex;
  }

  .sidebar {
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
    z-index: 101;
    transform: translateX(-100%);
    transition: transform 0.35s cubic-bezier(0.32, 0.72, 0, 1);
    box-shadow: none;
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .topbar {
    padding: 0 var(--spacing-12);
  }

  .chat-input-area {
    padding: var(--spacing-8) var(--spacing-12) var(--spacing-12);
  }
}
</style>
