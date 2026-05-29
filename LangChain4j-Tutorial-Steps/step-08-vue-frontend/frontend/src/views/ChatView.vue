<!-- ============================================================================
     ChatView.vue - 聊天主页面
     ============================================================================
     【教学要点】这是一个"容器组件"（Container Component），它的职责是：
       1. 定义页面布局结构（侧边栏 + 主聊天区域）
       2. 组装子组件（SessionList、MessageList、MessageInput、DocUpload）
       3. 不包含业务逻辑 —— 逻辑都在子组件和 Store 中

     【组件组合模式】
       ChatView (布局容器)
       ├── SessionList     (左侧边栏：会话列表)
       └── 右侧区域
           ├── MessageList  (消息展示区)
           └── 底部输入区
               ├── DocUpload     (文档上传)
               └── MessageInput  (消息输入框)

     【Element Plus 布局组件】
       使用 el-container / el-aside / el-header / el-main / el-footer
       实现经典的上-下-左-右布局结构，类似 HTML 的 flex 布局。

     【scoped 样式】
       <style scoped> 中的样式只作用于当前组件，不会泄漏到子组件。
       这是 Vue 的 Scoped CSS 特性，通过给元素添加 data-v-xxx 属性实现。
     ============================================================================ -->

<template>
  <!-- 最外层容器：水平布局（侧边栏 + 主区域） -->
  <el-container class="chat-container">
    <!-- 左侧边栏：会话列表 -->
    <el-aside width="260px" class="sidebar">
      <SessionList />
    </el-aside>

    <!-- 右侧主区域：垂直布局（头部 + 消息区 + 输入区） -->
    <el-container>
      <!-- 顶部标题栏 -->
      <el-header class="chat-header">
        <h2>SmartDoc 智能文档助手</h2>
      </el-header>

      <!-- 中间消息展示区 -->
      <el-main class="chat-main">
        <MessageList />
      </el-main>

      <!-- 底部输入区域 -->
      <el-footer height="auto" class="chat-footer">
        <DocUpload />
        <MessageInput />
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
// 导入子组件 —— 使用 <script setup> 语法，导入后直接在模板中使用
import SessionList from '@/components/SessionList.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import DocUpload from '@/components/DocUpload.vue'
</script>

<style scoped>
/* 全屏容器，高度占满视口 */
.chat-container {
  height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  border-right: 1px solid #e4e7ed;
  background-color: #f5f7fa;
}

/* 顶部标题栏 */
.chat-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fff;
}

.chat-header h2 {
  font-size: 18px;
  color: #303133;
}

/* 中间消息区域：可滚动 */
.chat-main {
  padding: 20px;
  overflow-y: auto;
  background-color: #fafafa;
}

/* 底部输入区域 */
.chat-footer {
  padding: 12px 20px;
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}
</style>
