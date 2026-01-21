<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>加载文档到向量数据库</h2>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <div class="info-section">
          <p class="info-text">
            📚 将从 <strong>resources/knowledge</strong> 目录加载文档到向量数据库
          </p>
          <p class="info-text">
            当前文档将被重新加载，用于 RAG 问答功能
          </p>
        </div>

        <div class="knowledge-info">
          <div class="info-item">
            <span class="icon">📁</span>
            <span class="text">目录: <code>src/main/resources/knowledge/</code></span>
          </div>
          <div class="info-item">
            <span class="icon">📄</span>
            <span class="text">支持格式: PDF, TXT, MD, DOCX</span>
          </div>
        </div>

        <div v-if="loading" class="loading-section">
          <div class="spinner"></div>
          <p>正在加载文档...</p>
        </div>

        <div v-if="result.message" :class="['result-section', result.type]">
          <p>{{ result.message }}</p>
        </div>
      </div>

      <div class="modal-footer">
        <button class="cancel-btn" @click="$emit('close')" :disabled="loading">
          取消
        </button>
        <button
          class="load-btn"
          @click="handleLoad"
          :disabled="loading"
        >
          {{ loading ? '加载中...' : '开始加载' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'
import * as chatApi from '../api/chat'

const emit = defineEmits(['close', 'documents-loaded'])

const loading = ref(false)
const result = ref({
  message: '',
  type: '' // 'success' or 'error'
})

const handleLoad = async () => {
  if (loading.value) return

  loading.value = true
  result.value = { message: '', type: '' }

  try {
    const response = await chatApi.loadDocuments()

    if (response.success) {
      result.value = {
        message: response.message || '文档加载成功！',
        type: 'success'
      }
      emit('documents-loaded')

      // 2秒后自动关闭
      setTimeout(() => {
        emit('close')
      }, 2000)
    } else {
      result.value = {
        message: response.message || '文档加载失败',
        type: 'error'
      }
    }
  } catch (error) {
    result.value = {
      message: error.response?.data?.message || error.message || '加载文档时发生错误',
      type: 'error'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s ease;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.info-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f0f7ff;
  border-left: 4px solid #667eea;
  border-radius: 6px;
}

.info-text {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.info-text:last-child {
  margin-bottom: 0;
}

.info-text strong {
  color: #667eea;
  font-weight: 600;
}

.knowledge-info {
  margin-bottom: 24px;
  padding: 16px;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .icon {
  font-size: 20px;
  flex-shrink: 0;
}

.info-item .text {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.info-item code {
  background: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  font-size: 13px;
  color: #667eea;
  border: 1px solid #e0e0e0;
}

.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 12px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.result-section {
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.result-section.success {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.result-section.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.result-section p {
  margin: 0;
  font-size: 14px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.load-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover:not(:disabled) {
  background: #e0e0e0;
}

.load-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.load-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.cancel-btn:disabled,
.load-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .modal-content {
    margin: 0;
    border-radius: 0;
    max-height: 100vh;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 16px;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .load-btn {
    width: 100%;
  }
}
</style>
