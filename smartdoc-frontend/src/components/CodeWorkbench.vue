<template>
  <div class="workbench">
    <div class="workbench-head">
      <div class="head-left">
        <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="12">
          <polyline points="16 18 22 12 16 6"/>
          <polyline points="8 6 2 12 8 18"/>
        </svg>
        <span class="head-title">实验台</span>
        <span class="head-lang" v-if="language">{{ language }}</span>
      </div>
      <div class="head-actions">
        <button class="action-btn" @click="copyCode" :title="copied ? 'Copied' : 'Copy'">
          <svg v-if="!copied" fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="11">
            <rect height="13" rx="2" ry="2" width="13" x="9" y="9"/>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
          </svg>
          <svg v-else fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="11">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <span>{{ copied ? 'Copied' : 'Copy' }}</span>
        </button>
        <button class="action-btn" @click="resetCode">
          <svg fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="11">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
            <path d="M3 3v5h5"/>
          </svg>
          <span>Reset</span>
        </button>
        <div class="action-divider"/>
        <span class="action-lines">{{ lineCount }} lines</span>
      </div>
    </div>

    <div class="workbench-editor">
      <div class="editor-gutter">
        <div v-for="n in lineCount" :key="n" class="gutter-line">{{ n }}</div>
      </div>
      <textarea
        ref="editorRef"
        :value="modelValue"
        class="editor-textarea"
        spellcheck="false"
        @input="handleInput"
        @keydown.tab.prevent="handleTab"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'

const props = defineProps<{
  modelValue: string
  language?: string
  originalCode: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const copied = ref(false)
const editorRef = ref<HTMLTextAreaElement>()

const lineCount = computed(() => props.modelValue.split('\n').length)

function handleInput(e: Event) {
  const target = e.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}

function handleTab(e: KeyboardEvent) {
  const ta = editorRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const val = ta.value
  const before = val.substring(0, start)
  const after = val.substring(end)
  ta.value = before + '  ' + after
  ta.selectionStart = ta.selectionEnd = start + 2
  emit('update:modelValue', ta.value)
}

function copyCode() {
  navigator.clipboard.writeText(props.modelValue).then(() => {
    copied.value = true
    setTimeout(() => copied.value = false, 1500)
  })
}

function resetCode() {
  emit('update:modelValue', props.originalCode)
}
</script>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  border-top: 1px solid #e5e5e7;
  background: #1d1d1f;
  height: 220px;
  min-height: 140px;
  flex-shrink: 0;
}

.workbench-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #2c2c2e;
  flex-shrink: 0;
  user-select: none;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #aeaeb2;
}

.head-title {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.head-lang {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 9px;
  font-weight: 500;
  color: #636366;
  background: #3a3a3c;
  padding: 1px 6px;
  border-radius: 3px;
  text-transform: uppercase;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  font-family: inherit;
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.action-btn:hover {
  background: #3a3a3c;
  color: #f5f5f7;
}

.action-divider {
  width: 1px;
  height: 14px;
  background: #48484a;
  margin: 0 4px;
}

.action-lines {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 10px;
  color: #636366;
}

.workbench-editor {
  flex: 1;
  display: flex;
  overflow: auto;
  position: relative;
}

.editor-gutter {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  min-width: 32px;
  background: #19191b;
  text-align: right;
  user-select: none;
  flex-shrink: 0;
}

.gutter-line {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 11px;
  line-height: 1.6;
  color: #48484a;
  padding: 0 8px;
}

.editor-textarea {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  color: #f5f5f7;
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 12px;
  line-height: 1.6;
  padding: 12px 16px;
  tab-size: 2;
  overflow: auto;
  white-space: pre;
}

.editor-textarea::selection {
  background: rgba(255, 255, 255, 0.12);
}

.editor-textarea::placeholder {
  color: #48484a;
}
</style>
