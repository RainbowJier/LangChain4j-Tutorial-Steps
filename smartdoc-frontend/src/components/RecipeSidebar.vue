<template>
  <aside class="sidebar">
    <div class="sidebar-head">
      <div class="sidebar-brand">
        <svg fill="none" height="14" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" viewBox="0 0 24 24" width="14">
          <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
          <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
        </svg>
        <span class="sidebar-title">Recipes</span>
      </div>
      <button class="sidebar-new" @click="handleNew">
        <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="12">
          <line x1="12" x2="12" y1="5" y2="19"/>
          <line x1="5" x2="19" y1="12" y2="12"/>
        </svg>
      </button>
    </div>

    <div class="sidebar-search">
      <svg fill="none" height="12" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="12">
        <circle cx="11" cy="11" r="8"/>
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
      <input v-model="searchQuery" class="search-input" placeholder="Search recipes…" type="text"/>
    </div>

    <div class="sidebar-divider"/>

    <nav class="sidebar-list">
      <div v-for="(group, gi) in filteredGroups" :key="gi" class="recipe-group">
        <button class="group-toggle" @click="toggleGroup(gi)">
          <svg :class="{ open: group.open }" fill="none" height="8" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="8">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
          <span class="group-name">{{ group.name }}</span>
          <span class="group-count">{{ group.items.length }}</span>
        </button>
        <TransitionGroup v-if="group.open" class="group-items" name="item-fade" tag="div">
          <button
            v-for="(recipe, ri) in group.items" :key="recipe.id"
            :class="{ active: recipe.id === activeRecipeId }"
            class="recipe-item"
            @click="selectRecipe(recipe.id)"
          >
            <span class="item-icon">
              <svg fill="none" height="10" stroke="currentColor" stroke-linecap="round" stroke-width="2" viewBox="0 0 24 24" width="10">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" x2="8" y1="13" y2="13"/>
                <line x1="16" x2="8" y1="17" y2="17"/>
              </svg>
            </span>
            <span class="item-title">{{ recipe.title }}</span>
            <span class="item-tag" v-if="recipe.tag">{{ recipe.tag }}</span>
          </button>
        </TransitionGroup>
      </div>
    </nav>

    <div class="sidebar-footer">
      <span class="footer-label">AI 生成的食谱会自动出现在这里</span>
    </div>
  </aside>
</template>

<script lang="ts" setup>
import {ref, computed} from 'vue'

const props = defineProps<{
  recipes: { id: string; title: string; tag?: string }[]
  activeRecipeId?: string
}>()

const emit = defineEmits<{
  select: [id: string]
  new: []
}>()

const searchQuery = ref('')
const openGroups = ref<Set<number>>(new Set([0]))

const groups = computed(() => {
  const all = props.recipes
  if (all.length === 0) return [{name: 'Recipes', items: [], open: true}]

  const cats = new Map<string, { name: string; items: typeof all; open: boolean }>()
  for (const r of all) {
    const cat = r.tag || 'General'
    if (!cats.has(cat)) cats.set(cat, {name: cat, items: [], open: openGroups.value.has([...cats.keys()].indexOf(cat))})
    cats.get(cat)!.items.push(r)
  }
  return Array.from(cats.values())
})

const filteredGroups = computed(() => {
  const q = searchQuery.value.toLowerCase().trim()
  if (!q) return groups.value
  return groups.value.map(g => ({
    ...g,
    items: g.items.filter(r => r.title.toLowerCase().includes(q))
  })).filter(g => g.items.length > 0)
})

function toggleGroup(gi: number) {
  if (openGroups.value.has(gi)) openGroups.value.delete(gi)
  else openGroups.value.add(gi)
}

function selectRecipe(id: string) {
  emit('select', id)
}

function handleNew() {
  emit('new')
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  background: #f5f5f7;
  border-right: 1px solid #e5e5e7;
  height: 100%;
  overflow: hidden;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  flex-shrink: 0;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1d1d1f;
}

.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.sidebar-new {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #86868b;
  cursor: pointer;
  transition: all 0.1s ease;
}

.sidebar-new:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.sidebar-search {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 12px 10px;
  padding: 6px 10px;
  background: #e8e8ed;
  border-radius: 8px;
  color: #86868b;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-family: inherit;
  font-size: 12px;
  color: #1d1d1f;
}

.search-input::placeholder {
  color: #aeaeb2;
}

.sidebar-divider {
  height: 1px;
  background: #e5e5e7;
  margin: 0 12px;
  flex-shrink: 0;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px;
}

.recipe-group {
  margin-bottom: 2px;
}

.group-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  padding: 6px 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #86868b;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: all 0.1s ease;
}

.group-toggle:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.group-toggle svg {
  transition: transform 0.15s ease;
  flex-shrink: 0;
}

.group-toggle svg.open {
  transform: rotate(90deg);
}

.group-name {
  flex: 1;
}

.group-count {
  font-size: 10px;
  color: #aeaeb2;
}

.group-items {
  overflow: hidden;
}

.recipe-item {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 6px 8px 6px 20px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #474747;
  font-size: 12px;
  cursor: pointer;
  text-align: left;
  transition: all 0.1s ease;
}

.recipe-item:hover {
  background: #e8e8ed;
  color: #1d1d1f;
}

.recipe-item.active {
  background: #1d1d1f;
  color: #ffffff;
}

.item-icon {
  display: flex;
  align-items: center;
  opacity: 0.4;
  flex-shrink: 0;
}

.recipe-item.active .item-icon {
  opacity: 0.7;
}

.item-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-tag {
  font-size: 9px;
  font-weight: 500;
  padding: 1px 6px;
  border-radius: 4px;
  background: #e8e8ed;
  color: #86868b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.sidebar-footer {
  padding: 10px 16px;
  border-top: 1px solid #e5e5e7;
  flex-shrink: 0;
}

.footer-label {
  font-size: 10px;
  color: #aeaeb2;
  line-height: 1.4;
}

.item-fade-enter-active, .item-fade-leave-active {
  transition: all 0.15s ease;
}

.item-fade-enter-from, .item-fade-leave-to {
  opacity: 0;
  transform: translateX(-4px);
}
</style>
