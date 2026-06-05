<template>
  <aside class="kb-sidebar">
    <div class="sidebar-section">
      <button
        :class="{ active: activeMode === 'home' }"
        class="nav-item"
        @click="$emit('navigate', 'home')"
      >
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" viewBox="0 0 24 24" width="14">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        <span>Home</span>
      </button>
      <button
        :class="{ active: activeMode === 'ask' }"
        class="nav-item"
        @click="$emit('navigate', 'ask')"
      >
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" viewBox="0 0 24 24" width="14">
          <circle cx="12" cy="12" r="10"/>
          <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
          <line x1="12" x2="12.01" y1="17" y2="17"/>
        </svg>
        <span>Ask AI</span>
      </button>
    </div>

    <div class="sidebar-divider"/>

    <div class="sidebar-section">
      <span class="section-label">Categories</span>
      <div class="category-list">
        <button
          v-for="cat in categories"
          :key="cat.id"
          :class="{ active: activeCategory === cat.id }"
          class="cat-item"
          @click="$emit('select-category', cat.id)"
        >
          <span class="cat-icon" v-html="cat.icon"/>
          <span class="cat-name">{{ cat.name }}</span>
          <span class="cat-count">{{ cat.docCount }}</span>
        </button>
      </div>
    </div>

    <div class="sidebar-divider"/>

    <div class="sidebar-section">
      <span class="section-label">Tags</span>
      <div class="tag-cloud">
        <button
          v-for="tag in tags"
          :key="tag"
          :class="{ active: activeTag === tag }"
          class="tag-pill"
          @click="$emit('select-tag', tag)"
        >
          #{{ tag }}
        </button>
      </div>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import type {KbCategory} from '@/types'

defineProps<{
  categories: KbCategory[]
  tags: string[]
  activeMode: string
  activeCategory?: string
  activeTag?: string
}>()

defineEmits<{
  navigate: [mode: string]
  'select-category': [id: string]
  'select-tag': [tag: string]
}>()
</script>

<style scoped>
.kb-sidebar {
  width: 220px;
  min-width: 220px;
  display: flex;
  flex-direction: column;
  background: var(--surface-card, #f5f5f7);
  border-right: 1px solid var(--color-silver-mist, #e5e5e7);
  height: 100%;
  overflow: hidden;
  padding: 12px 0;
}

.sidebar-section {
  padding: 0 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: all 0.12s ease;
  margin-bottom: 1px;
}

.nav-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.nav-item.active {
  background: #1d1d1f;
  color: #ffffff;
}

.sidebar-divider {
  height: 1px;
  background: #e5e5e7;
  margin: 10px 12px;
}

.section-label {
  display: block;
  padding: 6px 12px 4px;
  font-size: 10px;
  font-weight: 600;
  color: #aeaeb2;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #474747;
  font-family: inherit;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.1s ease;
}

.cat-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.cat-item.active {
  background: #e8e8ed;
  color: #1d1d1f;
  font-weight: 600;
}

.cat-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  opacity: 0.5;
  font-size: 14px;
}

.cat-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat-count {
  font-size: 10px;
  color: #aeaeb2;
  font-weight: 500;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px 12px 4px;
}

.tag-pill {
  font-family: inherit;
  font-size: 10px;
  font-weight: 500;
  color: #86868b;
  background: transparent;
  border: 1px solid #d2d2d7;
  border-radius: 999px;
  padding: 2px 10px;
  cursor: pointer;
  transition: all 0.1s ease;
}

.tag-pill:hover {
  color: #1d1d1f;
  border-color: #aeaeb2;
  background: #e8e8ed;
}

.tag-pill.active {
  color: #ffffff;
  background: #1d1d1f;
  border-color: #1d1d1f;
}
</style>
