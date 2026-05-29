<template>
  <div class="doc-upload">
    <el-upload
      :auto-upload="false"
      :on-change="handleFileChange"
      :show-file-list="false"
      accept=".txt,.pdf,.docx,.doc"
    >
      <el-button size="small" type="success" plain>Upload Document</el-button>
    </el-upload>

    <span v-if="uploading" class="upload-status">Uploading...</span>
    <span v-if="uploadSuccess" class="upload-success">Uploaded</span>
    <span v-if="uploadError" class="upload-error">{{ uploadError }}</span>

    <el-popover
      placement="top"
      :width="280"
      trigger="click"
      @show="loadDocuments"
    >
      <template #reference>
        <el-button size="small" type="info" plain>
          Documents ({{ chatStore.documents.length }})
        </el-button>
      </template>
      <div class="doc-list">
        <div v-if="chatStore.documents.length === 0" class="doc-empty">No documents uploaded</div>
        <div v-for="doc in chatStore.documents" :key="doc" class="doc-item">
          {{ doc }}
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { uploadDocument, listDocuments } from '@/api/chat'
import { useChatStore } from '@/stores/chat'
import type { UploadFile } from 'element-plus'

const chatStore = useChatStore()

const uploading = ref(false)
const uploadSuccess = ref(false)
const uploadError = ref('')

async function handleFileChange(file: UploadFile) {
  if (!file.raw) return

  uploading.value = true
  uploadSuccess.value = false
  uploadError.value = ''

  try {
    await uploadDocument(file.raw)
    uploadSuccess.value = true
    chatStore.documents.push(file.name)
    setTimeout(() => (uploadSuccess.value = false), 3000)
  } catch (e: any) {
    uploadError.value = e.message || 'Upload failed'
    setTimeout(() => (uploadError.value = ''), 5000)
  } finally {
    uploading.value = false
  }
}

async function loadDocuments() {
  try {
    const res = await listDocuments()
    chatStore.documents = res.documents || []
  } catch {
    // backend may not support listing
  }
}
</script>

<style scoped>
.doc-upload {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.upload-status {
  color: #909399;
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

.doc-list {
  max-height: 200px;
  overflow-y: auto;
}

.doc-empty {
  color: #c0c4cc;
  text-align: center;
  padding: 12px;
}

.doc-item {
  padding: 6px 4px;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}

.doc-item:last-child {
  border-bottom: none;
}
</style>
