<template>
  <div class="app-container">
    <Sidebar
      :current-mode="currentMode"
      :session-id="sessionId"
      :temperature="temperature"
      :max-tokens="maxTokens"
      @mode-change="handleModeChange"
      @settings-change="handleSettingsChange"
      @clear-chat="clearMessages"
      @show-tools="showToolsModal = true"
    />
    <ChatView
      :messages="messages"
      :current-mode="currentMode"
      :is-loading="isLoading"
      @send-message="sendMessage"
    />
    <McpToolsModal
      v-if="showToolsModal"
      @close="showToolsModal = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './components/Sidebar.vue'
import ChatView from './views/ChatView.vue'
import McpToolsModal from './components/McpToolsModal.vue'
import * as chatApi from './api/chat'

const currentMode = ref('simple')
const sessionId = ref('user-123')
const temperature = ref(0.7)
const maxTokens = ref(2000)
const messages = ref([])
const isLoading = ref(false)
const showToolsModal = ref(false)

const handleModeChange = (mode) => {
  currentMode.value = mode
}

const handleSettingsChange = (settings) => {
  sessionId.value = settings.sessionId
  temperature.value = settings.temperature
  maxTokens.value = settings.maxTokens
}

const clearMessages = () => {
  messages.value = []
}

const addMessage = (role, content, metadata = {}) => {
  messages.value.push({
    role,
    content,
    metadata,
    timestamp: new Date()
  })
}

const sendMessage = async (message) => {
  if (isLoading.value) return

  addMessage('user', message)
  isLoading.value = true

  try {
    const params = {
      message,
      sessionId: sessionId.value,
      temperature: temperature.value,
      maxTokens: maxTokens.value
    }

    switch (currentMode.value) {
      case 'simple':
        await handleSimpleChat(params)
        break
      case 'memory':
        await handleMemoryChat(params)
        break
      case 'stream':
        await handleStreamChat(params)
        break
      case 'rag':
        await handleRagChat(params)
        break
      case 'tool':
        await handleToolChat(params)
        break
      case 'mcp':
        await handleMcpChat(params)
        break
    }
  } catch (error) {
    addMessage('assistant', '抱歉，发生错误: ' + error.message, { error: true })
  } finally {
    isLoading.value = false
  }
}

const handleSimpleChat = async (params) => {
  const response = await chatApi.simpleChat(params)
  addMessage('assistant', response.content, {
    tokens: response.tokenUsageVO,
    model: response.modelName
  })
}

const handleMemoryChat = async (params) => {
  const response = await chatApi.memoryChat(params)
  addMessage('assistant', response.content, {
    tokens: response.tokenUsageVO,
    model: response.modelName,
    sessionId: params.sessionId
  })
}

const handleStreamChat = async (params) => {
  const messageIndex = messages.value.length
  addMessage('assistant', '', { streaming: true })

  await chatApi.streamChat(params, (token) => {
    messages.value[messageIndex].content += token
  })

  messages.value[messageIndex].metadata.streaming = false
}

const handleRagChat = async (params) => {
  const response = await chatApi.ragChat(params)
  addMessage('assistant', response.answer, {
    sources: response.sources,
    relevantDocs: response.relevantDocuments
  })
}

const handleToolChat = async (params) => {
  const response = await chatApi.toolChat(params)
  addMessage('assistant', response.response, {
    toolCalls: response.toolCalls,
    tokens: response.tokenUsage
  })
}

const handleMcpChat = async (params) => {
  const response = await chatApi.mcpChat(params)
  addMessage('assistant', response.response, {
    toolCalls: response.toolCalls,
    mcpTools: response.mcpToolsUsed
  })
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 100vh;
  overflow: hidden;
}

#app {
  height: 100vh;
}

.app-container {
  display: flex;
  height: 100vh;
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  gap: 20px;
}

@media (max-width: 1024px) {
  .app-container {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .app-container {
    padding: 10px;
  }
}
</style>
