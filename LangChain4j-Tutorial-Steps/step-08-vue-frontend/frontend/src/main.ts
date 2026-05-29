// ============================================================================
// main.ts - Vue 应用的入口文件
// ============================================================================
//
// 【教学要点】这是整个前端应用的启动点，职责是：
//   1. 创建 Vue 应用实例
//   2. 注册全局插件（Pinia 状态管理、Vue Router 路由、Element Plus UI）
//   3. 将应用挂载到 index.html 中的 #app 元素
//
// 插件注册顺序：Pinia -> Router -> ElementPlus（顺序一般不影响，但 Pinia
// 建议在 Router 之前，因为路由守卫中可能需要访问 store）
// ============================================================================

import { createApp } from 'vue'
import { createPinia } from 'pinia'       // 状态管理库，类似 Redux
import ElementPlus from 'element-plus'     // 基于 Vue 3 的 UI 组件库
import 'element-plus/dist/index.css'       // Element Plus 样式（必须导入）

import App from './App.vue'                // 根组件
import router from './router'              // 路由配置

const app = createApp(App)

// 注册插件 —— app.use() 会在 Vue 实例上安装插件提供的功能
app.use(createPinia())    // Pinia：提供全局响应式状态管理
app.use(router)           // Router：管理页面路由和导航
app.use(ElementPlus)      // ElementPlus：提供 el-button、el-input 等UI组件

// 将 Vue 应用挂载到 DOM 中的 #app 元素上，从此 Vue 接管该元素及其子树
app.mount('#app')
