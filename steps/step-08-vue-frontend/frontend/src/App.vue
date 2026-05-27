<!-- ============================================================================
     App.vue - 根组件
     ============================================================================
     【教学要点】App.vue 是 Vue 应用的最顶层组件，它只做一件事：
     渲染 <router-view />，即根据当前路由显示对应的页面组件。

     在本项目中只有一个路由 "/"，对应 ChatView.vue。
     如果将来增加更多页面（如设置页、登录页），只需在 router/index.ts
     中添加路由配置，<router-view /> 就会自动显示对应的组件。
     ============================================================================ -->

<template>
  <router-view />
</template>

<style>
/* 全局样式重置 —— 消除浏览器默认样式差异 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
