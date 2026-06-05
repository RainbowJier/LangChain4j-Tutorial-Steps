<template>
  <div class="cookbook">
    <header class="topbar">
      <div class="topbar-left">
        <div class="topbar-logo">
          <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="14">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
          </svg>
          <span class="topbar-brand">SmartDoc</span>
        </div>
        <span class="topbar-sep"/>
        <span class="topbar-session" v-if="sessionTitle">{{ sessionTitle }}</span>
      </div>
      <div class="topbar-right">
        <button class="topbar-btn" @click="createNewSession">
          <svg fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="11">
            <line x1="12" x2="12" y1="5" y2="19"/>
            <line x1="5" x2="19" y1="12" y2="12"/>
          </svg>
          <span>New Recipe</span>
        </button>
      </div>
    </header>

    <div class="layout-body">
      <RecipeSidebar
        :active-recipe-id="activeRecipeId"
        :recipes="recipeList"
        @select="scrollToRecipe"
        @new="createNewSession"
      />

      <div class="content-col">
        <main ref="contentRef" class="content-scroll">
          <div v-if="chatStore.currentMessages.length === 0" class="welcome">
            <div class="welcome-inner">
              <div class="welcome-icon">
                <svg fill="none" height="20" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" viewBox="0 0 24 24" width="20">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
              </div>
              <h1 class="welcome-title">What do you want to cook up today?</h1>
              <p class="welcome-sub">
                Ask me anything about Spring Boot, and I'll write you a recipe
              </p>
              <div class="welcome-input-area">
                <input
                  ref="welcomeInput"
                  v-model="inputText"
                  class="welcome-input"
                  placeholder="如何构建 REST API？"
                  @keydown="handleWelcomeKeydown"
                />
                <button
                  :class="{ visible: inputText.trim() }"
                  class="welcome-submit"
                  @click="sendFromWelcome"
                >
                  <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="14">
                    <line x1="12" x2="12" y1="19" y2="5"/>
                    <polyline points="5 12 12 5 19 12"/>
                  </svg>
                </button>
              </div>
              <div class="welcome-suggestions">
                <button
                  v-for="s in suggestions"
                  :key="s"
                  class="welcome-pill"
                  @click="sendMessage(s)"
                >
                  {{ s }}
                </button>
              </div>
            </div>
          </div>

          <div v-for="(msg, idx) in assistantMessages" :key="msg.id" :ref="el => setRecipeRef(msg.id, el as HTMLElement)">
            <RecipeCard
              :message="msg"
              :user-message="getUserMessage(idx)"
              @workbench="setWorkbenchCode"
            />
            <div v-if="idx < assistantMessages.length - 1" class="recipe-divider"/>
          </div>

          <div ref="bottomRef" class="bottom-sentinel"/>
        </main>

        <div v-if="chatStore.currentMessages.length > 0" class="bottom-panel">
          <CodeWorkbench
            v-if="workbenchCode !== null"
            v-model="workbenchCode"
            :language="workbenchLang"
            :original-code="workbenchOriginal"
          />

          <div class="input-bar">
            <div class="input-shell">
              <input
                ref="inputRef"
                v-model="inputText"
                class="input-field"
                placeholder="继续探索…"
                :disabled="chatStore.isLoading"
                @keydown="handleKeydown"
              />
              <button
                :class="{ active: inputText.trim() || chatStore.isLoading }"
                class="input-send"
                @click="handleSend"
              >
                <svg v-if="!chatStore.isLoading" fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="13">
                  <line x1="12" x2="12" y1="19" y2="5"/>
                  <polyline points="5 12 12 5 19 12"/>
                </svg>
                <svg v-else fill="currentColor" height="13" viewBox="0 0 24 24" width="13">
                  <rect height="10" rx="2" width="10" x="7" y="7"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, computed, watch, nextTick} from 'vue'
import {useChatStore} from '@/stores/chat'
import RecipeSidebar from '@/components/RecipeSidebar.vue'
import RecipeCard from '@/components/RecipeCard.vue'
import CodeWorkbench from '@/components/CodeWorkbench.vue'

const chatStore = useChatStore()
const contentRef = ref<HTMLElement>()
const inputRef = ref<HTMLInputElement>()
const welcomeInput = ref<HTMLInputElement>()
const bottomRef = ref<HTMLElement>()

const inputText = ref('')
const workbenchCode = ref<string | null>(null)
const workbenchOriginal = ref('')
const workbenchLang = ref('')
const activeRecipeId = ref('')

const recipeRefs = new Map<string, HTMLElement>()

const suggestions = [
  '如何构建 REST API？',
  'Spring Boot 连接 MySQL',
  'JWT 认证实现',
  '@Transactional 原理',
]

const assistantMessages = computed(() => {
  return chatStore.currentMessages.filter(m => m.role === 'assistant')
})

const sessionTitle = computed(() => {
  const s = chatStore.sessions.find(s => s.id === chatStore.currentSessionId)
  return s?.title || ''
})

const recipeList = computed(() => {
  return assistantMessages.value.map(m => {
    const title = extractTitle(m.content)
    return {id: m.id, title, tag: detectTag(title)}
  })
})

function extractTitle(content: string): string {
  const lines = content.split('\n').filter(l => l.trim())
  if (lines.length === 0) return 'Untitled'
  const first = lines[0] || 'Untitled'
  const clean = first.replace(/^#+\s*/, '').trim()
  return clean.length > 50 ? clean.slice(0, 47) + '...' : clean || 'Untitled'
}

function detectTag(title: string): string | undefined {
  const lower = title.toLowerCase()
  if (lower.includes('rest') || lower.includes('api') || lower.includes('controller')) return 'Controllers'
  if (lower.includes('jwt') || lower.includes('auth') || lower.includes('security') || lower.includes('login')) return 'Security'
  if (lower.includes('jpa') || lower.includes('mysql') || lower.includes('data') || lower.includes('database')) return 'Data'
  if (lower.includes('transaction') || lower.includes('aop') || lower.includes('cache')) return 'Advanced'
  return undefined
}

function getUserMessage(idx: number): string {
  const msgs = chatStore.currentMessages
  const assistantMsg = assistantMessages.value[idx]
  if (!assistantMsg) return ''
  const aiIdx = msgs.indexOf(assistantMsg)
  const prev = msgs[aiIdx - 1]
  if (aiIdx > 0 && prev?.role === 'user') {
    return prev.content
  }
  return ''
}

function setRecipeRef(id: string, el: HTMLElement | null) {
  if (el) recipeRefs.set(id, el)
}

function scrollToRecipe(id: string) {
  const el = recipeRefs.get(id)
  if (el) el.scrollIntoView({behavior: 'smooth', block: 'start'})
  activeRecipeId.value = id
}

function setWorkbenchCode(code: string) {
  workbenchCode.value = code
  workbenchOriginal.value = code
  workbenchLang.value = detectCodeLang(code)
}

function detectCodeLang(code: string): string {
  if (code.includes('@RestController') || code.includes('@Service') || code.includes('public class')) return 'Java'
  if (code.includes('<?xml') || code.includes('<dependency')) return 'XML'
  if (code.includes('spring:')) return 'YAML'
  return 'Java'
}

watch(
  () => assistantMessages.value.length,
  async (len, oldLen) => {
    if (len > oldLen && oldLen > 0) {
      await nextTick()
      bottomRef.value?.scrollIntoView({behavior: 'smooth'})
    }
  }
)

watch(
  () => assistantMessages.value,
  (msgs) => {
    const last = msgs[msgs.length - 1]
    if (last && !last.isStreaming) {
      extractAndSetWorkbench(last.content)
    }
  },
  {deep: true}
)

function extractAndSetWorkbench(content: string) {
  const lines = content.split('\n')
  let inCode = false
  let current: string[] = []
  for (const line of lines) {
    if (line.startsWith('```')) {
      if (inCode) {
        if (current.length > 0) {
          const code = current.join('\n')
          if (!workbenchCode.value || workbenchCode.value === workbenchOriginal.value) {
            workbenchCode.value = code
            workbenchOriginal.value = code
          }
          return
        }
        current = []
        inCode = false
      } else {
        inCode = true
      }
    } else if (inCode) {
      current.push(line)
    }
  }
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function handleWelcomeKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendFromWelcome()
  }
}

function sendFromWelcome() {
  const text = inputText.value.trim()
  if (!text) return
  sendMessage(text)
  inputText.value = ''
}

function handleSend() {
  if (chatStore.isLoading) {
    chatStore.stopStreaming()
    return
  }
  const text = inputText.value.trim()
  if (!text) return
  sendMessage(text)
  inputText.value = ''
}

function sendMessage(text: string) {
  chatStore.sendMessage(text)
  nextTick(() => bottomRef.value?.scrollIntoView({behavior: 'smooth'}))
}

function createNewSession() {
  chatStore.createSession()
  workbenchCode.value = null
  workbenchOriginal.value = ''
  activeRecipeId.value = ''
}
</script>

<style scoped>
.cookbook {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #ffffff;
  color: #1d1d1f;
}

/* ── Top Bar ──────────────────── */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 16px;
  background: #f5f5f7;
  border-bottom: 1px solid #e5e5e7;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar-logo {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1d1d1f;
}

.topbar-brand {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.topbar-sep {
  width: 1px;
  height: 14px;
  background: #d2d2d7;
}

.topbar-session {
  font-size: 12px;
  color: #86868b;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.topbar-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  font-family: inherit;
  font-size: 11px;
  font-weight: 500;
  color: #86868b;
  background: transparent;
  border: 1px solid #d2d2d7;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.topbar-btn:hover {
  color: #1d1d1f;
  border-color: #aeaeb2;
  background: #e8e8ed;
}

/* ── Layout ───────────────────── */
.layout-body {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.content-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 40px 24px;
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.recipe-divider {
  height: 1px;
  background: #e8e8ed;
  margin: 0;
}

.bottom-sentinel {
  height: 1px;
}

/* ── Welcome ──────────────────── */
.welcome {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  justify-content: center;
  padding: 60px 0;
}

.welcome-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  animation: fadeInUp 0.5s ease both;
}

.welcome-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: #f0f0f2;
  border-radius: 14px;
  color: #1d1d1f;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.48px;
  line-height: 1.15;
  color: #1d1d1f;
  margin-bottom: 10px;
}

.welcome-sub {
  font-size: 16px;
  line-height: 1.5;
  color: #86868b;
  max-width: 380px;
  margin-bottom: 36px;
}

.welcome-input-area {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 440px;
  padding: 4px 0;
  border-bottom: 1px solid #d2d2d7;
  transition: border-color 0.15s ease;
}

.welcome-input-area:focus-within {
  border-bottom-color: #1d1d1f;
}

.welcome-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 16px;
  color: #1d1d1f;
  padding: 8px 0;
  text-align: center;
}

.welcome-input::placeholder {
  color: #aeaeb2;
}

.welcome-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid #d2d2d7;
  border-radius: 50%;
  background: transparent;
  color: #aeaeb2;
  cursor: default;
  opacity: 0;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.welcome-submit.visible {
  opacity: 1;
  background: #1d1d1f;
  border-color: #1d1d1f;
  color: #ffffff;
  cursor: pointer;
}

.welcome-submit.visible:hover {
  opacity: 0.85;
}

.welcome-suggestions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 28px;
}

.welcome-pill {
  font-family: inherit;
  font-size: 12px;
  color: #86868b;
  background: #f0f0f2;
  border: 1px solid #e5e5e7;
  border-radius: 999px;
  padding: 5px 16px;
  cursor: pointer;
  transition: all 0.12s ease;
}

.welcome-pill:hover {
  color: #1d1d1f;
  border-color: #d2d2d7;
  background: #f5f5f7;
}

/* ── Bottom Panel ────────────── */
.bottom-panel {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.input-bar {
  flex-shrink: 0;
  padding: 14px 40px 20px;
  background: #ffffff;
  border-top: 1px solid #e8e8ed;
}

.input-shell {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 8px 8px 16px;
  background: #f5f5f7;
  border: 1px solid #e5e5e7;
  border-radius: 12px;
  transition: border-color 0.15s ease;
}

.input-shell:focus-within {
  border-color: #aeaeb2;
}

.input-field {
  flex: 1;
  border: none;
  outline: none;
  resize: none;
  background: transparent;
  font-family: inherit;
  font-size: 14px;
  color: #1d1d1f;
  line-height: 1.5;
}

.input-field::placeholder {
  color: #aeaeb2;
}

.input-send {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: #d2d2d7;
  color: #86868b;
  cursor: default;
  flex-shrink: 0;
  transition: all 0.1s ease;
}

.input-send.active {
  background: #1d1d1f;
  color: #ffffff;
  cursor: pointer;
}

.input-send.active:hover {
  opacity: 0.85;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
