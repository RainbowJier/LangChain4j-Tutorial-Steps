<!-- ============================================================================
     DocUpload.vue - 文档上传组件
     ============================================================================
     【教学要点】
       1. 使用 Element Plus 的 el-upload 组件处理文件选择
       2. :auto-upload="false" 禁用自动上传，改为手动控制上传逻辑
       3. 上传状态通过三个 ref 变量管理：uploading / uploadSuccess / uploadError
       4. setTimeout 控制成功/错误提示的自动消失
       5. FormData 是浏览器原生 API，用于构建 multipart/form-data 请求体
     ============================================================================ -->

<template>
  <div class="doc-upload">
    <!-- Element Plus 文件上传组件 -->
    <el-upload
      :auto-upload="false"          <!-- 不自动上传，由 handleFileChange 手动控制 -->
      :on-change="handleFileChange"  <!-- 文件选择变化时的回调 -->
      :show-file-list="false"        <!-- 不显示文件列表（自行管理状态提示） -->
      accept=".txt,.pdf,.docx,.doc"  <!-- 限制可选择的文件类型 -->
    >
      <el-button size="small" type="success" plain>上传文档</el-button>
    </el-upload>

    <!-- 状态提示文字 -->
    <span v-if="uploading" class="upload-status">上传中...</span>
    <span v-if="uploadSuccess" class="upload-success">上传成功</span>
    <span v-if="uploadError" class="upload-error">{{ uploadError }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadDocument } from '@/api/chat'
import type { UploadFile } from 'element-plus'

// 上传状态变量
const uploading = ref(false)
const uploadSuccess = ref(false)
const uploadError = ref('')

/** 文件选择变化时的处理函数 */
async function handleFileChange(file: UploadFile) {
  if (!file.raw) return

  uploading.value = true
  uploadSuccess.value = false
  uploadError.value = ''

  try {
    await uploadDocument(file.raw)
    uploadSuccess.value = true
    // 3 秒后自动隐藏成功提示
    setTimeout(() => (uploadSuccess.value = false), 3000)
  } catch (e: any) {
    uploadError.value = e.message || '上传失败'
    // 5 秒后自动隐藏错误提示
    setTimeout(() => (uploadError.value = ''), 5000)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.doc-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-status {
  color: #409eff;
  font-size: 13px;
}

.upload-success {
  color: #67c23a;
  font-size: 13px;
}

.upload-error {
  color: #f56c6c;
  font-size: 13px;
}
</style>
