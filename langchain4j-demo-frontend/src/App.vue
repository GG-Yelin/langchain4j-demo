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
      @show-local-tools="showLocalToolsModal = true"
      @show-tools="showToolsModal = true"
    />
    <ChatView
      :messages="messages"
      :current-mode="currentMode"
      :is-loading="isLoading"
      @send-message="sendMessage"
      @send-assistant-message="sendAssistantMessage"
    />
    <LocalToolsModal
      v-if="showLocalToolsModal"
      @close="showLocalToolsModal = false"
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
import LocalToolsModal from './components/LocalToolsModal.vue'
import McpToolsModal from './components/McpToolsModal.vue'
import * as chatApi from './api/chat'

const currentMode = ref('simple')
const sessionId = ref('user-123')
const temperature = ref(0.7)
const maxTokens = ref(2000)
const messages = ref([])
const isLoading = ref(false)
const showLocalToolsModal = ref(false)
const showToolsModal = ref(false)

const handleModeChange = (mode) => {
  // 如果模式发生变化，清空聊天记录（开始新会话）
  if (currentMode.value !== mode) {
    messages.value = []
  }
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
      case 'assistant':
        // AI助手模式在 sendAssistantMessage 中单独处理
        break
    }
  } catch (error) {
    addMessage('assistant', '抱歉，发生错误: ' + error.message, { error: true })
  } finally {
    isLoading.value = false
  }
}

const sendAssistantMessage = async (data) => {
  if (isLoading.value) return

  isLoading.value = true

  try {
    let userDisplayMessage = ''
    let response = null

    switch (data.type) {
      case 'simple':
        userDisplayMessage = data.message
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChat({ message: data.message })
        addMessage('assistant', response.response, {
          type: 'simple-assistant',
          tokens: response.tokenUsage
        })
        break

      case 'custom':
        userDisplayMessage = `[系统提示词: ${data.systemMessage}]\n${data.message}`
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChatCustom({
          systemMessage: data.systemMessage,
          message: data.message
        })
        addMessage('assistant', response.response, {
          type: 'custom-assistant',
          tokens: response.tokenUsage
        })
        break

      case 'variables':
        userDisplayMessage = `[语言: ${data.language}] [话题: ${data.topic}]`
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChatVariables({
          language: data.language,
          topic: data.topic
        })
        addMessage('assistant', response.response, {
          type: 'variables-assistant',
          tokens: response.tokenUsage
        })
        break

      case 'simple-tools':
        userDisplayMessage = data.message
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChatWithTools({ message: data.message })
        addMessage('assistant', response.response, {
          type: 'simple-tools-assistant',
          tokens: response.tokenUsage,
          toolCalls: response.toolExecutions
        })
        break

      case 'custom-tools':
        userDisplayMessage = `[系统提示词: ${data.systemMessage}]\n${data.message}`
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChatWithToolsCustom({
          systemMessage: data.systemMessage,
          message: data.message
        })
        addMessage('assistant', response.response, {
          type: 'custom-tools-assistant',
          tokens: response.tokenUsage,
          toolCalls: response.toolExecutions
        })
        break

      case 'variables-tools':
        userDisplayMessage = `[语言: ${data.language}] [话题: ${data.topic}]`
        addMessage('user', userDisplayMessage)
        response = await chatApi.assistantChatWithToolsVariables({
          language: data.language,
          topic: data.topic
        })
        addMessage('assistant', response.response, {
          type: 'variables-tools-assistant',
          tokens: response.tokenUsage,
          toolCalls: response.toolExecutions
        })
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

  try {
    // 注意：由于 LangChain4j 1.0.0-beta3 的流式传输在处理 UTF-8 时存在编码问题，
    // 这里使用简单聊天接口 + 前端模拟流式显示的方案，以保证中文正常显示
    const response = await chatApi.simpleChat(params)

    if (response.content) {
      // 逐字显示内容，模拟流式效果
      const fullText = response.content
      let currentIndex = 0

      const intervalId = setInterval(() => {
        if (currentIndex < fullText.length) {
          messages.value[messageIndex].content = fullText.substring(0, currentIndex + 1)
          currentIndex++
        } else {
          clearInterval(intervalId)
          messages.value[messageIndex].metadata.streaming = false
          messages.value[messageIndex].metadata.tokens = response.tokenUsageVO
        }
      }, 30) // 每30ms显示一个字符，模拟打字效果
    }
  } catch (error) {
    messages.value[messageIndex].content = '发生错误: ' + error.message
    messages.value[messageIndex].metadata.streaming = false
    messages.value[messageIndex].metadata.error = true
  }
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
  addMessage('assistant', response.content, {
    toolCalls: response.toolExecutions,
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
