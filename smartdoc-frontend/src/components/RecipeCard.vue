<template>
  <article class="recipe-card">
    <div class="recipe-context" v-if="userMessage">
      <span class="context-icon">Q</span>
      <span class="context-text">{{ userMessage }}</span>
    </div>

    <div class="recipe-body markdown-body" v-html="renderedContent"/>

    <div v-if="message.isStreaming" class="stream-indicator">
      <span class="stream-dot"/>
      <span class="stream-text">Generating recipe…</span>
    </div>

    <div v-if="codeBlocks.length > 0 && !message.isStreaming" class="recipe-send-to-workbench">
      <button class="send-btn" @click="sendToWorkbench">
        <svg fill="none" height="11" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="11">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="7 10 12 15 17 10"/>
          <line x1="12" y1="15" x2="12" y2="3"/>
        </svg>
        Open in Workbench
      </button>
    </div>
  </article>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import type {ChatMessage} from '@/types'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import json from 'highlight.js/lib/languages/json'

hljs.registerLanguage('java', java)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('json', json)

const props = defineProps<{
  message: ChatMessage
  userMessage: string
}>()

const emit = defineEmits<{
  workbench: [code: string]
}>()

const md = new MarkdownIt({
  html: false, linkify: true, breaks: false, typographer: true,
  highlight(str: string, lang: string): string {
    const detected = lang && hljs.getLanguage(lang) ? lang : (hljs.highlightAuto(str).language || '')
    const highlighted = detected ? hljs.highlight(str, {language: detected, ignoreIllegals: true}).value : md.utils.escapeHtml(str)
    const lines = str.split('\n').length
    const langLabel = detected || 'code'
    return `<div class="code-block">
      <div class="code-header">
        <span class="code-lang">${langLabel}</span>
        <span class="code-lines">${lines} lines</span>
        <button class="code-copy" onclick="(function(btn){
          const code = btn.closest('.code-block').querySelector('code').innerText;
          navigator.clipboard.writeText(code);
          btn.textContent = 'Copied!';
          setTimeout(function(){ btn.textContent = 'Copy'; }, 1500);
        })(this)">Copy</button>
      </div>
      <code class="hljs">${highlighted}</code>
    </div>`
  }
})

const defaultLinkOpen = md.renderer.rules.link_open
  || ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.link_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const codeBlocks = computed(() => {
  const lines = props.message.content.split('\n')
  const blocks: string[] = []
  let inCode = false
  let current: string[] = []
  for (const line of lines) {
    if (line.startsWith('```')) {
      if (inCode) {
        blocks.push(current.join('\n'))
        current = []
        inCode = false
      } else {
        inCode = true
      }
    } else if (inCode) {
      current.push(line)
    }
  }
  return blocks
})

function sanitizeIncomplete(text: string): string {
  const count = (text.match(/```/g) || []).length
  if (count % 2 !== 0) return text + '\n```'
  return text
}

const renderedContent = computed(() => {
  const raw = props.message.isStreaming ? sanitizeIncomplete(props.message.content) : props.message.content
  const html = md.render(raw)
  return props.message.isStreaming ? html + '<span class="cursor"></span>' : html
})

function sendToWorkbench() {
  if (codeBlocks.value.length > 0) {
    emit('workbench', codeBlocks.value[0] || '')
  }
}
</script>

<style scoped>
.recipe-card {
  padding: 32px 0;
  animation: fadeInUp 0.4s cubic-bezier(0.25, 0.1, 0.25, 1) both;
}

.recipe-context {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 20px;
}

.context-icon {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 11px;
  font-weight: 600;
  color: #aeaeb2;
  background: #f0f0f2;
  padding: 2px 7px;
  border-radius: 4px;
  flex-shrink: 0;
}

.context-text {
  font-size: 14px;
  color: #86868b;
  line-height: 1.5;
}

/* ── Markdown Body ─────────────── */
.recipe-body :deep(p) {
  margin: 0 0 12px;
}

.recipe-body :deep(p:last-child) {
  margin-bottom: 0;
}

.recipe-body :deep(h1),
.recipe-body :deep(h2),
.recipe-body :deep(h3),
.recipe-body :deep(h4) {
  font-weight: 600;
  color: #1d1d1f;
  margin: 24px 0 10px;
  line-height: 1.3;
  letter-spacing: -0.01em;
}

.recipe-body :deep(h1) { font-size: 26px; }
.recipe-body :deep(h2) { font-size: 22px; border-bottom: 1px solid #e8e8ed; padding-bottom: 8px; }
.recipe-body :deep(h3) { font-size: 18px; }
.recipe-body :deep(h4) { font-size: 16px; }

.recipe-body :deep(h2:first-child), .recipe-body :deep(h3:first-child) {
  margin-top: 0;
}

.recipe-body :deep(ul), .recipe-body :deep(ol) {
  padding-left: 20px;
  margin: 8px 0 12px;
}

.recipe-body :deep(li) {
  margin-bottom: 4px;
  line-height: 1.6;
}

.recipe-body :deep(blockquote) {
  margin: 12px 0;
  padding: 12px 16px;
  border-left: 3px solid #1d1d1f;
  background: #f5f5f7;
  border-radius: 0 8px 8px 0;
  color: #474747;
  font-size: 14px;
}

.recipe-body :deep(blockquote p) {
  margin: 0;
}

.recipe-body :deep(code:not(.hljs)) {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 0.85em;
  background: #f0f0f2;
  color: #1d1d1f;
  padding: 1px 5px;
  border-radius: 4px;
}

.recipe-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8ed;
  margin: 20px 0;
}

.recipe-body :deep(strong) {
  font-weight: 600;
}

.recipe-body :deep(em) {
  font-style: italic;
}

.recipe-body :deep(a) {
  color: #0066cc;
  text-decoration: none;
  font-weight: 500;
}

.recipe-body :deep(a:hover) {
  text-decoration: underline;
}

/* ── Code Blocks ──────────────── */
.recipe-body :deep(.code-block) {
  margin: 16px 0;
  background: #1d1d1f;
  border-radius: 12px;
  overflow: hidden;
}

.recipe-body :deep(.code-header) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  background: #2c2c2e;
  user-select: none;
}

.recipe-body :deep(.code-lang) {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.recipe-body :deep(.code-lines) {
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 10px;
  color: #636366;
  margin-right: auto;
}

.recipe-body :deep(.code-copy) {
  font-family: inherit;
  font-size: 10px;
  font-weight: 500;
  color: #aeaeb2;
  background: #3a3a3c;
  border: none;
  border-radius: 4px;
  padding: 2px 8px;
  cursor: pointer;
  opacity: 0;
  transition: all 0.1s ease;
}

.recipe-body :deep(.code-block:hover .code-copy) {
  opacity: 1;
}

.recipe-body :deep(.code-copy:hover) {
  background: #48484a;
  color: #ffffff;
}

.recipe-body :deep(.code-block code.hljs) {
  display: block;
  padding: 14px 16px;
  font-family: 'SF Mono', 'JetBrains Mono', monospace;
  font-size: 12px;
  line-height: 1.7;
  overflow-x: auto;
  color: #f5f5f7;
  tab-size: 2;
}

.recipe-body :deep(.hljs-keyword) { color: #ff7b72; }
.recipe-body :deep(.hljs-string) { color: #a5d6ff; }
.recipe-body :deep(.hljs-comment) { color: #8b949e; font-style: italic; }
.recipe-body :deep(.hljs-type) { color: #ffa657; }
.recipe-body :deep(.hljs-number) { color: #79c0ff; }
.recipe-body :deep(.hljs-built_in) { color: #d2a8ff; }
.recipe-body :deep(.hljs-title) { color: #ffa657; }
.recipe-body :deep(.hljs-attr) { color: #79c0ff; }
.recipe-body :deep(.hljs-tag) { color: #ff7b72; }
.recipe-body :deep(.hljs-name) { color: #ff7b72; }

.recipe-body :deep(.cursor) {
  display: inline-block;
  width: 2px;
  height: 1.1em;
  background: #1d1d1f;
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 0.8s steps(2) infinite;
}

/* ── Streaming ────────────────── */
.stream-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding: 10px 14px;
  background: #f5f5f7;
  border-radius: 8px;
}

.stream-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1d1d1f;
  animation: pulse 1.2s ease infinite;
}

.stream-text {
  font-size: 12px;
  color: #86868b;
}

/* ── Send to Workbench ────────── */
.recipe-send-to-workbench {
  margin-top: 16px;
}

.send-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-family: inherit;
  font-size: 12px;
  font-weight: 500;
  color: #1d1d1f;
  background: #f5f5f7;
  border: 1px solid #d2d2d7;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.send-btn:hover {
  background: #e8e8ed;
  border-color: #aeaeb2;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
