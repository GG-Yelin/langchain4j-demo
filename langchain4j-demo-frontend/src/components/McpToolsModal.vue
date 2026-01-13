<template>
  <div class="modal" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>可用的 MCP 工具</h2>
        <span class="close" @click="$emit('close')">&times;</span>
      </div>
      <div class="modal-body">
        <div v-if="loading">
          <p>加载中...</p>
        </div>
        <div v-else-if="error">
          <p style="color: #d32f2f;">{{ error }}</p>
        </div>
        <div v-else-if="tools.length === 0">
          <p>暂无可用的MCP工具</p>
        </div>
        <div v-else>
          <div
            v-for="(tool, index) in tools"
            :key="index"
            class="tool-item"
          >
            <h3>{{ tool.name || 'Unknown Tool' }}</h3>
            <p>{{ tool.description || '无描述' }}</p>
            <div v-if="tool.parameters" class="tool-params">
              参数: {{ JSON.stringify(tool.parameters, null, 2) }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineEmits } from 'vue'
import { getMcpTools } from '../api/chat'

const emit = defineEmits(['close'])

const tools = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    tools.value = await getMcpTools()
  } catch (err) {
    error.value = '加载失败: ' + err.message
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.modal {
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  animation: fadeIn 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  width: 80%;
  max-width: 800px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 20px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
}

.close {
  color: white;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.3s ease;
  line-height: 1;
}

.close:hover {
  transform: scale(1.2);
}

.modal-body {
  padding: 30px;
  max-height: calc(80vh - 80px);
  overflow-y: auto;
}

.tool-item {
  padding: 15px;
  margin-bottom: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.tool-item h3 {
  color: #667eea;
  margin-bottom: 8px;
  font-size: 16px;
}

.tool-item p {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.tool-params {
  font-size: 12px;
  color: #999;
  font-family: monospace;
  background: white;
  padding: 8px;
  border-radius: 4px;
  margin-top: 8px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
