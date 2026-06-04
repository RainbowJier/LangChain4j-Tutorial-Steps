<template>
  <div class="input-bar">
    <div :class="{ focused, disabled: chatStore.isLoading }" class="input-shell">
      <textarea
          id="chat-input" ref="textareaRef" v-model="inputText"
          :disabled="chatStore.isLoading"
          :placeholder="chatStore.isLoading ? 'Waiting for response…' : 'Ask anything about Java & Spring…'" aria-label="Chat input"
          class="input-field" rows="1"
          @blur="focused = false" @focus="focused = true"
          @input="autoResize" @keydown="handleKeydown"
      />
      <button id="send-btn" :class="{ active: inputText.trim(), loading: chatStore.isLoading }"
              :disabled="!inputText.trim() && !chatStore.isLoading" aria-label="Send message"
              class="send-btn" @click="handleSend">
        <svg v-if="chatStore.isLoading" class="stop-icon" fill="currentColor" height="14" viewBox="0 0 24 24"
             width="14">
          <rect height="12" rx="2" width="12" x="6" y="6"/>
        </svg>
        <svg v-else class="send-icon" fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="2.5" viewBox="0 0 24 24" width="16">
          <line x1="12" x2="12" y1="19" y2="5"/>
          <polyline points="5 12 12 5 19 12"/>
        </svg>
      </button>
    </div>
    <div class="input-footer">
      <kbd>Enter</kbd><span>send</span><kbd>⇧ Enter</kbd><span>newline</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, nextTick} from 'vue'
import {useChatStore} from '@/stores/chat'

const chatStore = useChatStore()
const inputText = ref('')
const focused = ref(false)
const textareaRef = ref<HTMLTextAreaElement>()

function autoResize() {
  const el = textareaRef.value;
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    handleSend()
  }
}

function handleSend() {
  if (chatStore.isLoading) {
    chatStore.stopStreaming();
    return
  }
  const text = inputText.value.trim();
  if (!text) return
  chatStore.sendMessage(text);
  inputText.value = ''
  nextTick(() => {
    if (textareaRef.value) textareaRef.value.style.height = 'auto'
  })
}
</script>

<style scoped>
.input-bar {
  width: 100%;
  max-width: var(--chat-max-width);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-shell {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-8);
  padding: var(--spacing-12) var(--spacing-12) var(--spacing-12) var(--spacing-20);
  background: rgba(250, 250, 252, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--color-silver-mist);
  border-radius: var(--radius-cards);
  transition: border-color var(--duration-fast) var(--ease-primary);
}

.input-shell.focused {
  border-color: var(--color-azure);
}

.input-shell.disabled {
  opacity: 0.5;
}

.input-field {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-family: var(--font-text);
  font-size: var(--text-body);
  font-weight: var(--weight-regular);
  color: var(--color-ink);
  max-height: 160px;
  overflow-y: auto;
  padding: 4px 0;
  line-height: 1.5;
  letter-spacing: var(--tracking-body);
}

.input-field::placeholder {
  color: var(--color-graphite);
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: var(--color-silver-mist);
  color: var(--color-graphite);
  cursor: default;
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-primary);
}

.send-btn.active {
  background: var(--color-azure);
  color: var(--color-snow);
  cursor: pointer;
}

.send-btn.active:hover {
  background: #0077ed;
}

.send-btn.active:active {
  transform: scale(0.92);
}

.send-btn.loading {
  background: var(--color-ink);
  color: var(--color-snow);
  cursor: pointer;
}

.send-btn.loading:hover {
  background: var(--color-obsidian);
}

.send-icon, .stop-icon {
  display: block;
}

.input-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-4);
  font-size: var(--text-caption);
  color: var(--color-graphite);
  letter-spacing: var(--tracking-caption);
}

.input-footer kbd {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 17px;
  padding: 0 4px;
  font-size: 10px;
  font-family: var(--font-text);
  font-weight: var(--weight-medium);
  color: var(--color-graphite);
  background: var(--surface-card);
  border: 1px solid var(--color-silver-mist);
  border-radius: 3px;
  line-height: 1;
}
</style>
