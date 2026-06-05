<template>
  <div ref="listRef" class="msg-scroll">

    <div v-if="chatStore.currentMessages.length === 0" class="welcome">
      <div class="welcome-inner">
        <div class="welcome-hero">
          <div class="welcome-icon">
            <svg fill="none" height="24" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8"
                 viewBox="0 0 24 24" width="24">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h1 class="welcome-title">How can I help you today?</h1>
          <p class="welcome-sub">
            Your AI assistant for Java, Spring Boot &amp; enterprise architecture
          </p>
        </div>

        <div class="quick-actions">
          <button class="quick-card"
                  @click="chatStore.sendMessage('How do I create a new Spring Boot project from scratch?')">
            <div class="quick-card-icon">
              <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="16">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <div class="quick-card-body">
              <span class="quick-card-title">Spring Boot Setup</span>
              <span class="quick-card-desc">Initialize a new Maven project from scratch</span>
            </div>
            <div class="quick-card-arrow">
              <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="13">
                <line x1="5" x2="19" y1="12" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </div>
          </button>
          <button class="quick-card"
                  @click="chatStore.sendMessage('Explain how Spring dependency injection and the IoC container work')">
            <div class="quick-card-icon">
              <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="16">
                <rect height="7" width="7" x="3" y="3"/>
                <rect height="7" width="7" x="14" y="3"/>
                <rect height="7" width="7" x="14" y="14"/>
                <rect height="7" width="7" x="3" y="14"/>
              </svg>
            </div>
            <div class="quick-card-body">
              <span class="quick-card-title">Dependency Injection</span>
              <span class="quick-card-desc">Understand IoC and bean lifecycle</span>
            </div>
            <div class="quick-card-arrow">
              <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="13">
                <line x1="5" x2="19" y1="12" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </div>
          </button>
          <button class="quick-card"
                  @click="chatStore.sendMessage('How do I build a production-ready RESTful API with Spring MVC?')">
            <div class="quick-card-icon">
              <svg fill="none" height="16" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="16">
                <polyline points="16 18 22 12 16 6"/>
                <polyline points="8 6 2 12 8 18"/>
              </svg>
            </div>
            <div class="quick-card-body">
              <span class="quick-card-title">REST API Design</span>
              <span class="quick-card-desc">Controllers, mappings &amp; responses</span>
            </div>
            <div class="quick-card-arrow">
              <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                   width="13">
                <line x1="5" x2="19" y1="12" y2="12"/>
                <polyline points="12 5 19 12 12 19"/>
              </svg>
            </div>
          </button>
        </div>
      </div>
    </div>

    <TransitionGroup v-else class="thread" name="msg-fade" tag="div">
      <div
          v-for="msg in chatStore.currentMessages"
          :key="msg.id"
          :class="['msg-block', msg.role]"
      >
        <div v-if="msg.role === 'user'" class="user-msg">
          <span class="user-text">{{ msg.content }}</span>
        </div>

        <div v-else class="ai-msg">
          <div class="ai-header">
            <div class="ai-avatar">
              <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                   viewBox="0 0 24 24" width="13">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <span class="ai-name">SmartDoc</span>
          </div>
          <div
              class="markdown-body animate-fade-in"
              v-html="msg.isStreaming ? streamingHtml : getRenderedContent(msg)"
          />
          <div v-if="!msg.isStreaming" class="msg-footer">
            <button class="copy-btn" @click="copyMessage(msg.content, $event)">
              <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                   viewBox="0 0 24 24" width="12">
                <rect height="13" rx="2" ry="2" width="13" x="9" y="9"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              <span>Copy</span>
            </button>
          </div>
        </div>
      </div>
    </TransitionGroup>
  </div>

  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="activeZoomSvg" class="zoom-modal" @click="closeZoom">
        <div class="zoom-modal-content" @click.stop>
          <header class="zoom-modal-header">
            <div class="zoom-modal-title">
              <svg fill="none" height="15" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="15">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" x2="16.65" y1="21" y2="16.65"/>
              </svg>
              <span>Diagram Viewer</span>
            </div>
            <div class="zoom-modal-actions">
              <button class="zoom-ctrl-btn" title="Reset" @click="resetZoom">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                     width="13">
                  <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                  <path d="M3 3v5h5"/>
                </svg>
              </button>
              <button class="zoom-ctrl-btn" @click="zoomScale = Math.max(0.5, zoomScale - 0.25)">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                     width="13">
                  <line x1="5" x2="19" y1="12" y2="12"/>
                </svg>
              </button>
              <span class="scale-label">{{ Math.round(zoomScale * 100) }}%</span>
              <button class="zoom-ctrl-btn" @click="zoomScale = Math.min(3, zoomScale + 0.25)">
                <svg fill="none" height="13" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24"
                     width="13">
                  <line x1="12" x2="12" y1="5" y2="19"/>
                  <line x1="5" x2="19" y1="12" y2="12"/>
                </svg>
              </button>
              <div class="zoom-divider"></div>
              <button class="zoom-ctrl-btn accent" @click="downloadModalPng">PNG</button>
              <button class="zoom-ctrl-btn" @click="downloadModalSvg">SVG</button>
              <div class="zoom-divider"></div>
              <button class="zoom-close-btn" @click="closeZoom">
                <svg fill="none" height="15" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24" width="15">
                  <line x1="18" x2="6" y1="6" y2="18"/>
                  <line x1="6" x2="18" y1="6" y2="18"/>
                </svg>
              </button>
            </div>
          </header>
          <div
              class="zoom-viewport"
              style="cursor: grab; user-select: none;"
              @mousedown="handleMouseDown"
              @mouseleave="handleMouseUp"
              @mousemove="handleMouseMove"
              @mouseup="handleMouseUp"
              @wheel.prevent="handleWheel"
          >
            <div
                :style="{ transform: `translate(${panX}px, ${panY}px) scale(${zoomScale})` }"
                class="zoom-wrapper"
                v-html="activeZoomSvg"
            />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script lang="ts" setup>
import {ref, watch, nextTick, onMounted, onUnmounted} from 'vue'
import {useChatStore} from '@/stores/chat'
import type {ChatMessage} from '@/types'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import json from 'highlight.js/lib/languages/json'
import bash from 'highlight.js/lib/languages/bash'
import sql from 'highlight.js/lib/languages/sql'
import yaml from 'highlight.js/lib/languages/yaml'
import go from 'highlight.js/lib/languages/go'
import mermaid from 'mermaid'

mermaid.initialize({
  theme: 'base',
  themeVariables: {
    primaryColor: '#f5f5f7',
    primaryTextColor: '#1d1d1f',
    primaryBorderColor: '#d2d2d7',
    lineColor: '#aeaeb2',
    secondaryColor: '#ffffff',
    tertiaryColor: '#f5f5f7',
    background: '#ffffff',
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
    fontSize: '13px',
  },
  flowchart: {
    useMaxWidth: true,
    htmlLabels: true,
    curve: 'basis',
    padding: 16,
    nodeSpacing: 50,
    rankSpacing: 80,
  },
  sequence: {
    diagramMarginX: 50, diagramMarginY: 30, actorMargin: 80,
    width: 200, height: 65, boxMargin: 20, boxTextMargin: 8,
    noteMargin: 20, messageMargin: 60, mirrorActors: false,
    bottomMarginAdj: 10, useMaxWidth: true, rightAngles: true,
  },
  startOnLoad: false,
})

hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript);
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('python', python);
hljs.registerLanguage('py', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('html', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash);
hljs.registerLanguage('sh', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('yaml', yaml);
hljs.registerLanguage('yml', yaml)
hljs.registerLanguage('go', go)

const chatStore = useChatStore()
const listRef = ref<HTMLElement>()

const htmlCache = new Map<string, string>()
const streamingHtml = ref('')

let lastStreamingMsgId = ''
let throttleTimeout: any = null
let pendingContent = ''
let isStreamingMsg = false

watch(
    () => chatStore.currentSessionId,
    () => {
      htmlCache.clear()
      if (throttleTimeout) {
        clearTimeout(throttleTimeout)
        throttleTimeout = null
      }
      lastStreamingMsgId = ''
      streamingHtml.value = ''
    }
)

watch(() => chatStore.currentMessages.length, scrollToBottom)
watch(() => chatStore.currentMessages[chatStore.currentMessages.length - 1]?.content, scrollToBottom)

watch(
    () => chatStore.currentMessages,
    (newMessages) => {
      const lastMsg = newMessages[newMessages.length - 1]
      if (lastMsg && lastMsg.role === 'assistant' && lastMsg.isStreaming) {
        lastStreamingMsgId = lastMsg.id
        pendingContent = lastMsg.content

        if (!throttleTimeout) {
          streamingHtml.value = renderContent(lastMsg)

          throttleTimeout = setTimeout(() => {
            throttleTimeout = null
            const msgToRender = chatStore.currentMessages.find(m => m.id === lastStreamingMsgId)
            if (msgToRender) {
              streamingHtml.value = renderContent(msgToRender)
            }
          }, 60)
        }
      } else {
        if (throttleTimeout) {
          clearTimeout(throttleTimeout)
          throttleTimeout = null
        }
        lastStreamingMsgId = ''
        streamingHtml.value = ''
      }
    },
    {deep: true}
)

function normalizeMarkdown(text: string): string {
  return text
      .replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
      .replace(/([^\n])(\s*#{2,6}\s)/g, '$1\n\n$2')
      .replace(/([^\n])(\s*(?:[-*+]\s|(?:\d+\.)\s))/g, '$1\n$2')
      .replace(/([^\n])(\s*`{3,})/g, '$1\n\n$2')
      .replace(/(`{3,}[a-z]*\n?)([^\n])/g, '$1\n$2')
}

let mermaidIdCounter = 0
let currentRenderingMsgId = ''

const md = new MarkdownIt({
  html: false, linkify: true, breaks: false, typographer: true,
  highlight(str: string, lang: string): string {
    if (lang === 'mermaid') {
      if (isStreamingMsg) {
        return `<div class="mermaid-container mermaid-streaming">
          <div class="code-header">
            <span class="code-lang">mermaid</span>
            <span class="code-line-count">Rendering...</span>
          </div>
          <pre class="mermaid-fallback">${md.utils.escapeHtml(str)}</pre>
        </div>`
      }
      const id = `mermaid-${++mermaidIdCounter}`
      const msgId = currentRenderingMsgId
      setTimeout(() => renderMermaid(msgId, id, str), 0)
      return `<div class="mermaid-container">
        <div class="mermaid-toolbar">
          <button class="mermaid-tool-btn zoom-btn" title="Zoom & Drag" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
            Zoom
          </button>
          <button class="mermaid-tool-btn export-png-btn" title="Export PNG" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            PNG
          </button>
          <button class="mermaid-tool-btn export-svg-btn" title="Export SVG" type="button">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v8"/><path d="m8 12 4 4 4-4"/>
            </svg>
            SVG
          </button>
        </div>
        <div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(str)}</div>
      </div>`
    }
    const hl = (code: string, l: string) => {
      try {
        return hljs.highlight(code, {language: l, ignoreIllegals: true}).value
      } catch {
        return md.utils.escapeHtml(code)
      }
    }
    const detected = lang && hljs.getLanguage(lang) ? lang : (hljs.highlightAuto(str).language || '')
    const highlighted = detected ? hl(str, detected) : md.utils.escapeHtml(str)
    const langLabel = detected || 'text'
    const lines = str.split('\n').length
    return [
      `<div class="code-block">`,
      `<div class="code-header">`,
      `<span class="code-lang">${langLabel}</span>`,
      `<span class="code-line-count">${lines} lines</span>`,
      `<button class="code-copy-btn" type="button">Copy</button>`,
      `</div>`,
      `<code class="hljs">${highlighted}</code>`,
      `</div>`
    ].join('')
  }
})

const MERMAID_CLASS_NAMES = ['warning', 'decision', 'highlight', 'success', 'primary', 'default', 'accent', 'error', 'warn', 'info', 'sub']

function preprocessMermaid(definition: string): string {
  let clean = definition.replace(/\r\n/g, '\n').replace(/\r/g, '\n')
  clean = clean.replace(/%%\{init:[\s\S]*?\}%%/gi, '')
  const lines = clean.split('\n')
  const processedLines: string[] = []
  for (let i = 0; i < lines.length; i++) {
    let line = lines[i]
    if (line === undefined) continue
    let t = line.trim()
    if (!t || t.startsWith('%%')) continue
    t = t.replace(/\b(flowchart|graph)\s*(TD|TB|LR|BT|RL)\b/gi, '$1 $2')
    t = t.replace(/\b(direction)\s*(TB|BT|LR|RL|TD)\b/gi, '$1 $2')
    t = t.replace(/\bsubgraph\s*([A-Za-z0-9_\u4e00-\u9fff\[")])/gi, 'subgraph $1')
    t = t.replace(/\bclassDef\s*([a-zA-Z_][a-zA-Z0-9_]*)\s*(fill|stroke|color|font-size|fontFamily|fontStyle|stroke-width|stroke-dasharray|border-radius)/gi, 'classDef $1 $2')
    if (/^\s*class\s*[A-Z]/.test(t) && !/^\s*classDef/.test(t)) {
      const s = t.replace(/^\s*class\s+/, 'class')
      if (s.endsWith(';')) {
        const body = s.slice(5, -1)
        let fixed = false
        for (const cn of MERMAID_CLASS_NAMES) {
          const idx = body.lastIndexOf(cn)
          if (idx !== -1 && idx + cn.length === body.length) {
            t = `class ${body.slice(0, idx)} ${cn};`
            fixed = true
            break
          }
        }
        if (!fixed) t = s.slice(0, 5) + ' ' + s.slice(5)
      }
    }
    t = t.replace(/-->/g, ' --> ')
    t = t.replace(/>\|/g, '> |')
    t = t.replace(/\|"/g, '| "')
    processedLines.push(t)
  }
  return processedLines.join('\n')
}

async function renderMermaid(msgId: string, id: string, definition: string) {
  const el = document.getElementById(id)
  if (!el) return
  const preprocessed = preprocessMermaid(definition)
  try {
    const {svg} = await mermaid.render(id + '-svg', preprocessed)
    el.innerHTML = svg
    if (msgId) {
      const cachedHtml = htmlCache.get(msgId)
      if (cachedHtml) {
        const placeholder = `<div id="${id}" class="mermaid-svg">${md.utils.escapeHtml(definition)}</div>`
        const replacement = `<div id="${id}" class="mermaid-svg">${svg}</div>`
        if (cachedHtml.includes(placeholder)) {
          htmlCache.set(msgId, cachedHtml.replace(placeholder, replacement))
        }
      }
    }
  } catch (err) {
    console.error('Mermaid render error:', err)
    el.innerHTML = `<pre class="mermaid-fallback">${md.utils.escapeHtml(definition)}</pre>`
  }
}

const defaultLinkOpen = md.renderer.rules.link_open ||
    ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.link_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

const defaultTableOpen = md.renderer.rules.table_open ||
    ((tokens: any, idx: any, options: any, _env: any, self: any) => self.renderToken(tokens, idx, options))
md.renderer.rules.table_open = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return '<div class="table-scroll">' + defaultTableOpen(tokens, idx, options, env, self)
}
const defaultTableClose = md.renderer.rules.table_close ||
    ((_tokens: any, _idx: any, _options: any, _env: any, _self: any) => '</table>')
md.renderer.rules.table_close = (tokens: any, idx: any, options: any, env: any, self: any) => {
  return defaultTableClose(tokens, idx, options, env, self) + '</div>'
}

function sanitizeIncomplete(text: string): string {
  const codeFenceCount = (text.match(/```/g) || []).length
  if (codeFenceCount % 2 !== 0) {
    return text + '\n```'
  }
  return text
}

function renderContent(msg: ChatMessage): string {
  isStreamingMsg = !!msg.isStreaming
  currentRenderingMsgId = msg.id
  const raw = msg.isStreaming ? sanitizeIncomplete(msg.content) : msg.content
  const normalized = normalizeMarkdown(raw)
  const html = md.render(normalized)
  currentRenderingMsgId = ''
  isStreamingMsg = false
  return msg.isStreaming ? html + '<span class="stream-cursor"></span>' : html
}

function getRenderedContent(msg: ChatMessage): string {
  if (htmlCache.has(msg.id)) {
    return htmlCache.get(msg.id)!
  }
  const html = renderContent(msg)
  htmlCache.set(msg.id, html)
  return html
}

function copyMessage(content: string, e: MouseEvent) {
  const btn = e.currentTarget as HTMLButtonElement
  navigator.clipboard.writeText(content).then(() => {
    btn.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg> <span>Copied</span>`
    setTimeout(() => {
      btn.innerHTML = `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg> <span>Copy</span>`
    }, 1500)
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (listRef.value) listRef.value.scrollTo({top: listRef.value.scrollHeight, behavior: 'smooth'})
  })
}

function handleCopyClick(e: MouseEvent) {
  const btn = (e.target as HTMLElement).closest<HTMLButtonElement>('.code-copy-btn')
  if (!btn) return
  const block = btn.closest('.code-block')
  const code = block?.querySelector('code')?.innerText ?? ''
  navigator.clipboard.writeText(code).then(() => {
    btn.textContent = 'Copied!'
    setTimeout(() => {
      btn.textContent = 'Copy'
    }, 1500)
  })
}

const activeZoomSvg = ref('')
const zoomScale = ref(1.0)
const panX = ref(0)
const panY = ref(0)

let isDragging = false
let startX = 0
let startY = 0

function handleMouseDown(e: MouseEvent) {
  if (e.button !== 0) return
  isDragging = true
  startX = e.clientX - panX.value
  startY = e.clientY - panY.value
  const viewport = e.currentTarget as HTMLElement
  viewport.style.cursor = 'grabbing'
}

function handleMouseMove(e: MouseEvent) {
  if (!isDragging) return
  panX.value = e.clientX - startX
  panY.value = e.clientY - startY
}

function handleMouseUp(e: MouseEvent) {
  isDragging = false
  const viewport = e.currentTarget as HTMLElement
  if (viewport) viewport.style.cursor = 'grab'
}

function resetZoom() {
  zoomScale.value = 1.0
  panX.value = 0
  panY.value = 0
}

const ZOOM_STEP = 0.1
const ZOOM_MIN = 0.25
const ZOOM_MAX = 5

function handleWheel(e: WheelEvent) {
  const viewport = e.currentTarget as HTMLElement
  const rect = viewport.getBoundingClientRect()
  const mouseX = e.clientX - rect.left - rect.width / 2
  const mouseY = e.clientY - rect.top - rect.height / 2
  const prevScale = zoomScale.value
  const delta = e.deltaY < 0 ? ZOOM_STEP : -ZOOM_STEP
  const newScale = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, prevScale + delta))
  const scaleDiff = newScale / prevScale
  panX.value = mouseX - scaleDiff * (mouseX - panX.value)
  panY.value = mouseY - scaleDiff * (mouseY - panY.value)
  zoomScale.value = newScale
}

function closeZoom() {
  activeZoomSvg.value = ''
  resetZoom()
}

function handleMermaidClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  const zoomBtn = target.closest<HTMLButtonElement>('.zoom-btn')
  if (zoomBtn) {
    const container = zoomBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) {
      activeZoomSvg.value = svgEl.outerHTML;
      resetZoom()
    }
    return
  }
  const exportPngBtn = target.closest<HTMLButtonElement>('.export-png-btn')
  if (exportPngBtn) {
    const container = exportPngBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) triggerPngDownload(svgEl.outerHTML, 'mermaid-diagram.png')
    return
  }
  const exportSvgBtn = target.closest<HTMLButtonElement>('.export-svg-btn')
  if (exportSvgBtn) {
    const container = exportSvgBtn.closest('.mermaid-container')
    let svgEl = container?.querySelector('.mermaid-svg svg')
    if (!svgEl && container) {
      const allSvgs = container.querySelectorAll('svg')
      for (const s of Array.from(allSvgs)) {
        if (!s.closest('.mermaid-toolbar')) {
          svgEl = s;
          break
        }
      }
    }
    if (svgEl) triggerSvgDownload(svgEl.outerHTML, 'mermaid-diagram.svg')
    return
  }
}

function triggerSvgDownload(svgContent: string, filename: string) {
  const blob = new Blob([svgContent], {type: 'image/svg+xml;charset=utf-8'})
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url;
  a.download = filename
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function triggerPngDownload(svgContent: string, filename: string) {
  const img = new Image()
  const svgBlob = new Blob([svgContent], {type: 'image/svg+xml;charset=utf-8'})
  const url = URL.createObjectURL(svgBlob)
  img.onload = () => {
    const canvas = document.createElement('canvas')
    const scaleFactor = 2
    const originalWidth = img.naturalWidth || 800
    const originalHeight = img.naturalHeight || 600
    canvas.width = originalWidth * scaleFactor
    canvas.height = originalHeight * scaleFactor
    const ctx = canvas.getContext('2d')
    if (ctx) {
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvas.width, canvas.height)
      ctx.scale(scaleFactor, scaleFactor)
      ctx.drawImage(img, 0, 0)
      try {
        const pngUrl = canvas.toDataURL('image/png')
        const a = document.createElement('a')
        a.href = pngUrl;
        a.download = filename
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a)
      } catch (err) {
        console.error('Failed to export canvas to PNG:', err)
        triggerSvgDownload(svgContent, filename.replace('.png', '.svg'))
      }
    }
    URL.revokeObjectURL(url)
  }
  img.onerror = () => {
    triggerSvgDownload(svgContent, filename.replace('.png', '.svg'));
    URL.revokeObjectURL(url)
  }
  img.src = url
}

function downloadModalSvg() {
  if (activeZoomSvg.value) triggerSvgDownload(activeZoomSvg.value, 'mermaid-diagram.svg')
}

function downloadModalPng() {
  if (activeZoomSvg.value) triggerPngDownload(activeZoomSvg.value, 'mermaid-diagram.png')
}

onMounted(() => {
  if (!listRef.value) return
  listRef.value.addEventListener('click', handleCopyClick)
  listRef.value.addEventListener('click', handleMermaidClick)
})

onUnmounted(() => {
  if (listRef.value) {
    listRef.value.removeEventListener('click', handleCopyClick)
    listRef.value.removeEventListener('click', handleMermaidClick)
  }
})
</script>

<style scoped>
.msg-scroll {
  height: 100%;
  overflow-y: auto;
  padding: var(--spacing-24) 0;
  scroll-behavior: smooth;
}

/* ── Welcome ─────────────────────────────────────────── */
.welcome {
  max-width: var(--chat-max-width);
  margin: 0 auto;
  padding: 120px 24px 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.welcome-inner {
  width: 100%;
  max-width: 600px;
}

.welcome-hero {
  text-align: center;
  margin-bottom: var(--spacing-48);
}

.welcome-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--surface-card);
  color: var(--color-ink);
  margin-bottom: var(--spacing-24);
  border: 1px solid var(--color-silver-mist);
}

.welcome-title {
  font-family: var(--font-text);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  letter-spacing: var(--tracking-display);
  line-height: var(--leading-display);
  color: var(--color-ink);
  margin-bottom: var(--spacing-16);
}

.welcome-sub {
  font-size: var(--text-subheading);
  font-weight: var(--weight-light);
  letter-spacing: var(--tracking-subheading);
  line-height: var(--leading-subheading);
  color: var(--color-graphite);
  max-width: 480px;
  margin: 0 auto;
}

/* ── Quick Actions ───────────────────────────────────── */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: var(--spacing-12);
}

.quick-card {
  display: flex;
  gap: var(--spacing-12);
  align-items: flex-start;
  text-align: left;
  padding: var(--card-padding);
  background: var(--surface-card);
  border: 1px solid var(--color-silver-mist);
  border-radius: var(--radius-cards);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-primary);
}

.quick-card:hover {
  border-color: var(--color-azure);
  background: rgba(0, 113, 227, 0.02);
}

.quick-card:active {
  transform: scale(0.97);
}

.quick-card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--surface-recessed);
  color: var(--color-ink);
  flex-shrink: 0;
}

.quick-card-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.quick-card-title {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-body-sm);
  color: var(--color-ink);
}

.quick-card-desc {
  font-size: var(--text-caption);
  letter-spacing: var(--tracking-caption);
  color: var(--color-graphite);
  line-height: 1.45;
}

.quick-card-arrow {
  display: flex;
  align-items: center;
  margin-left: auto;
  color: var(--color-graphite);
  flex-shrink: 0;
  margin-top: 6px;
}

/* ── Thread ──────────────────────────────────────────── */
.thread {
  max-width: var(--chat-max-width);
  margin: 0 auto;
  padding: 0 var(--spacing-24);
}

.msg-block {
  margin-bottom: var(--spacing-28);
  will-change: transform, opacity;
}

.msg-fade-enter-active {
  transition: all 0.344s var(--ease-primary);
}

.msg-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.msg-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.msg-fade-move {
  transition: transform 0.344s var(--ease-primary);
}

/* ── User Message ────────────────────────────────────── */
.user-msg {
  display: inline-flex;
  padding: var(--spacing-12) var(--spacing-20);
  background: var(--color-ink);
  border-radius: 20px 20px 6px 20px;
  max-width: 72%;
  width: fit-content;
  margin-left: auto;
}

.user-text {
  white-space: pre-wrap;
  font-family: var(--font-text);
  color: var(--color-snow);
  font-size: var(--text-body);
  font-weight: var(--weight-regular);
  letter-spacing: var(--tracking-body);
  line-height: 1.55;
}

/* ── AI Message ──────────────────────────────────────── */
.ai-msg {
  padding: 4px 0 var(--spacing-16);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
  margin-bottom: var(--spacing-8);
  user-select: none;
}

.ai-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: var(--radius-sm);
  background: var(--surface-card);
  color: var(--color-ink);
  border: 1px solid var(--color-silver-mist);
}

.ai-name {
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  letter-spacing: var(--tracking-body-sm);
  color: var(--color-ink);
}

.msg-footer {
  display: flex;
  gap: var(--spacing-8);
  margin-top: var(--spacing-12);
  opacity: 0;
  transition: opacity var(--duration-fast) var(--ease-primary);
}

.msg-block:hover .msg-footer {
  opacity: 1;
}

.copy-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px var(--spacing-8);
  font-family: var(--font-text);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--color-graphite);
  background: var(--surface-card);
  border: 1px solid var(--color-silver-mist);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-primary);
}

.copy-btn:hover {
  color: var(--color-azure);
  border-color: var(--color-azure);
}

/* ── Stream Cursor ───────────────────────────────────── */
.markdown-body :deep(.stream-cursor) {
  display: inline-block;
  width: 2px;
  height: 1.15em;
  background: var(--color-ink);
  margin-left: 2px;
  vertical-align: text-bottom;
  border-radius: 1px;
  animation: blink 0.8s steps(2) infinite;
}

/* ── Markdown Body ───────────────────────────────────── */
.markdown-body {
  font-family: var(--font-text);
  font-size: var(--text-body);
  font-weight: var(--weight-regular);
  line-height: var(--leading-body);
  letter-spacing: var(--tracking-body);
  color: var(--color-ink);
  word-break: break-word;
}

.markdown-body :deep(p) {
  margin: 0 0 var(--spacing-8);
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  font-weight: var(--weight-semibold);
  color: var(--color-ink);
  margin: var(--spacing-28) 0 var(--spacing-8);
  line-height: 1.3;
  letter-spacing: -0.02em;
}

.markdown-body :deep(h1) {
  font-size: 28px;
}

.markdown-body :deep(h2) {
  font-size: 24px;
  margin-top: var(--spacing-32);
  border-bottom: 1px solid var(--color-silver-mist);
  padding-bottom: var(--spacing-8);
}

.markdown-body :deep(h3) {
  font-size: var(--text-heading-sm);
}

.markdown-body :deep(h4) {
  font-size: var(--text-subheading);
}

.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  font-size: var(--text-body);
}

.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child),
.markdown-body :deep(h4:first-child) {
  margin-top: 0;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--color-silver-mist);
  margin: var(--spacing-20) 0;
}

.markdown-body :deep(blockquote) {
  margin: var(--spacing-12) 0;
  padding: var(--spacing-12) var(--spacing-16);
  border-left: 2px solid var(--color-graphite);
  background: var(--surface-recessed);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-graphite);
}

.markdown-body :deep(blockquote p) {
  margin: 0;
}

.markdown-body :deep(ul), .markdown-body :deep(ol) {
  padding-left: 20px;
  margin: var(--spacing-8) 0 var(--spacing-12);
}

.markdown-body :deep(li) {
  margin-bottom: var(--spacing-4);
}

.markdown-body :deep(li > ul), .markdown-body :deep(li > ol) {
  margin: var(--spacing-4) 0;
}

.markdown-body :deep(code) {
  font-family: var(--font-mono);
  font-size: 0.85em;
  background: var(--surface-recessed);
  color: var(--color-ink);
  padding: 1px 5px;
  border-radius: 4px;
}

/* ── Code Blocks ─────────────────────────────────────── */
.markdown-body :deep(.code-block) {
  background: #1d1d1f;
  border-radius: var(--radius-cards);
  overflow: hidden;
  margin: var(--spacing-16) 0;
  border: none;
}

.markdown-body :deep(.code-header) {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
  padding: 8px var(--spacing-16);
  background: #2c2c2e;
  user-select: none;
}

.markdown-body :deep(.code-lang) {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: var(--weight-medium);
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.markdown-body :deep(.code-line-count) {
  font-family: var(--font-mono);
  font-size: 10px;
  color: #636366;
  margin-right: auto;
}

.markdown-body :deep(.code-copy-btn) {
  font-family: var(--font-text);
  font-size: 11px;
  font-weight: var(--weight-medium);
  color: #aeaeb2;
  background: #3a3a3c;
  border: none;
  border-radius: 4px;
  padding: 3px var(--spacing-8);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-primary);
  opacity: 0;
}

.markdown-body :deep(.code-block:hover .code-copy-btn) {
  opacity: 1;
}

.markdown-body :deep(.code-copy-btn:hover) {
  background: #48484a;
  color: #ffffff;
}

.markdown-body :deep(.code-block code.hljs) {
  display: block;
  background: transparent;
  color: #f5f5f7;
  padding: var(--spacing-16);
  font-size: 13px;
  line-height: 1.7;
  overflow-x: auto;
  border: none;
  border-radius: 0;
  tab-size: 2;
  font-family: var(--font-mono);
}

.markdown-body :deep(pre:not(.code-block)) {
  background: #1d1d1f;
  color: #f5f5f7;
  padding: var(--spacing-12) var(--spacing-16);
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin: var(--spacing-12) 0;
  border: none;
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.6;
}

.markdown-body :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
  border: none;
  border-radius: 0;
  font-size: inherit;
}

/* Apple-style light syntax highlighting for dark code blocks */
.markdown-body :deep(.hljs-keyword) {
  color: #ff7b72;
}

.markdown-body :deep(.hljs-built_in) {
  color: #d2a8ff;
}

.markdown-body :deep(.hljs-type) {
  color: #ffa657;
}

.markdown-body :deep(.hljs-literal) {
  color: #79c0ff;
}

.markdown-body :deep(.hljs-number) {
  color: #79c0ff;
}

.markdown-body :deep(.hljs-string) {
  color: #a5d6ff;
}

.markdown-body :deep(.hljs-comment) {
  color: #8b949e;
  font-style: italic;
}

.markdown-body :deep(.hljs-function) {
  color: #d2a8ff;
}

.markdown-body :deep(.hljs-title) {
  color: #ffa657;
}

.markdown-body :deep(.hljs-attr) {
  color: #79c0ff;
}

.markdown-body :deep(.hljs-variable) {
  color: #ffa657;
}

.markdown-body :deep(.hljs-tag) {
  color: #ff7b72;
}

.markdown-body :deep(.hljs-name) {
  color: #ff7b72;
}

.markdown-body :deep(.hljs-meta) {
  color: #d2a8ff;
}

.markdown-body :deep(.hljs-operator) {
  color: #ff7b72;
}

.markdown-body :deep(.hljs-params) {
  color: #f5f5f7;
}

/* ── Links ───────────────────────────────────────────── */
.markdown-body :deep(a) {
  color: var(--color-cobalt-link);
  text-decoration: none;
  font-weight: var(--weight-medium);
  transition: opacity var(--duration-fast) var(--ease-primary);
}

.markdown-body :deep(a:hover) {
  opacity: 0.75;
  text-decoration: underline;
}

.markdown-body :deep(strong) {
  color: var(--color-ink);
  font-weight: var(--weight-semibold);
}

.markdown-body :deep(em) {
  font-style: italic;
}

/* ── Tables ──────────────────────────────────────────── */
.markdown-body :deep(.table-scroll) {
  width: 100%;
  overflow-x: auto;
  margin: var(--spacing-16) 0;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-silver-mist);
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-body-sm);
  min-width: 400px;
}

.markdown-body :deep(thead) {
  background: var(--surface-recessed);
}

.markdown-body :deep(th) {
  padding: var(--spacing-8) var(--spacing-12);
  text-align: left;
  font-weight: var(--weight-medium);
  font-size: var(--text-caption);
  color: var(--color-graphite);
  border-bottom: 1px solid var(--color-silver-mist);
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.markdown-body :deep(td) {
  padding: var(--spacing-8) var(--spacing-12);
  border-bottom: 1px solid var(--color-silver-mist);
  color: var(--color-ink);
}

.markdown-body :deep(tr:last-child td) {
  border-bottom: none;
}

.markdown-body :deep(tr:hover td) {
  background: var(--surface-recessed);
}

/* ── Images ──────────────────────────────────────────── */
.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: var(--spacing-16) 0;
}

/* ── Mermaid ─────────────────────────────────────────── */
.markdown-body :deep(.mermaid-container) {
  position: relative;
  margin: var(--spacing-16) 0;
  background: var(--surface-card);
  border-radius: var(--radius-cards);
  border: 1px solid var(--color-silver-mist);
  overflow: visible;
  transition: border-color var(--duration-fast) var(--ease-primary);
}

.markdown-body :deep(.mermaid-container:hover) {
  border-color: var(--color-azure);
}

.markdown-body :deep(.mermaid-toolbar) {
  position: absolute;
  top: var(--spacing-8);
  right: var(--spacing-8);
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity var(--duration-fast) var(--ease-primary);
  z-index: 5;
}

.markdown-body :deep(.mermaid-container:hover .mermaid-toolbar) {
  opacity: 1;
}

.markdown-body :deep(.mermaid-tool-btn) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 5px 10px;
  font-family: var(--font-text);
  font-size: 11px;
  font-weight: var(--weight-medium);
  line-height: 1;
  color: var(--color-graphite);
  background: var(--surface-card);
  border: 1px solid var(--color-silver-mist);
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--duration-fast) var(--ease-primary);
}

.markdown-body :deep(.mermaid-tool-btn:hover) {
  color: var(--color-ink);
  border-color: var(--color-azure);
  background: rgba(0, 122, 255, 0.06);
}

.markdown-body :deep(.mermaid-svg) {
  overflow: auto;
  max-height: 70vh;
  padding: var(--spacing-20);
  -webkit-overflow-scrolling: touch;
}

.markdown-body :deep(.mermaid-svg svg) {
  display: block;
  max-width: 100%;
  width: fit-content;
  min-width: min(100%, 400px);
  height: auto;
  font-family: var(--font-text);
}

.markdown-body :deep(.mermaid-fallback) {
  font-family: var(--font-mono);
  font-size: var(--text-body-sm);
  color: var(--color-graphite);
  white-space: pre-wrap;
}
.markdown-body :deep(.mermaid-tool-btn.active) {
  color: #ffffff;
  background: #1d1d1f;
  border-color: #1d1d1f;
}

/* ── Zoom Modal ──────────────────────────────────────── */
.zoom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.zoom-modal-content {
  width: 90vw;
  height: 85vh;
  background: var(--surface-card);
  border-radius: var(--radius-cards);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalScaleUp 0.344s var(--ease-primary) forwards;
}

@keyframes sourceFadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes modalScaleUp {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.zoom-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-12) var(--spacing-20);
  background: var(--surface-card);
  border-bottom: 1px solid var(--color-silver-mist);
}

.zoom-modal-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-8);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  color: var(--color-ink);
}

.zoom-modal-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.zoom-ctrl-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px var(--spacing-8);
  font-family: var(--font-text);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  color: var(--color-graphite);
  background: var(--surface-card);
  border: 1px solid var(--color-silver-mist);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-primary);
}

.zoom-ctrl-btn:hover {
  border-color: var(--color-ink);
  color: var(--color-ink);
}

.zoom-ctrl-btn.accent {
  background: var(--color-azure);
  border-color: var(--color-azure);
  color: var(--color-snow);
}

.zoom-ctrl-btn.accent:hover {
  background: #0077ed;
}

.scale-label {
  font-family: var(--font-mono);
  font-size: var(--text-caption);
  color: var(--color-graphite);
  min-width: 40px;
  text-align: center;
}

.zoom-divider {
  width: 1px;
  height: 16px;
  background: var(--color-silver-mist);
}

.zoom-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  color: var(--color-graphite);
  background: transparent;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-primary);
}

.zoom-close-btn:hover {
  background: var(--surface-recessed);
  color: var(--color-ink);
}

.zoom-viewport {
  flex: 1;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-32);
  background: var(--surface-card);
}

.zoom-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  transform-origin: center center;
  width: 100%;
  height: 100%;
}

.zoom-wrapper :deep(svg) {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
}

.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}
</style>
