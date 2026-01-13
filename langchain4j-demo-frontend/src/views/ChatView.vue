<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>LangChain4j 聊天演示</h1>
      <div class="mode-indicator">
        当前模式: <span>{{ getModeLabel(currentMode) }}</span>
      </div>
    </div>

    <div class="messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="welcome-message">
        <h2>欢迎使用 LangChain4j 聊天演示!</h2>
        <p>请从左侧选择聊天模式，然后开始对话。</p>
        <ul>
          <li><strong>简单聊天:</strong> 基础的单次对话</li>
          <li><strong>记忆聊天:</strong> 支持多轮对话的上下文记忆</li>
          <li><strong>流式聊天:</strong> 实时流式输出响应</li>
          <li><strong>RAG 问答:</strong> 基于文档检索的问答</li>
          <li><strong>工具调用:</strong> AI自动调用内置工具</li>
          <li><strong>MCP 工具:</strong> 使用MCP协议调用外部工具</li>
        </ul>
      </div>

      <MessageItem
        v-for="(message, index) in messages"
        :key="index"
        :message="message"
      />
    </div>

    <div class="input-area">
      <textarea
        v-model="inputMessage"
        placeholder="输入消息..."
        rows="3"
        @keydown.enter.exact.prevent="handleSend"
        ref="inputRef"
      ></textarea>
      <button
        @click="handleSend"
        :disabled="isLoading || !inputMessage.trim()"
        class="send-btn"
      >
        <div v-if="isLoading" class="loading"></div>
        <span v-else>发送</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, defineProps, defineEmits } from 'vue'
import MessageItem from '../components/MessageItem.vue'

const props = defineProps({
  messages: Array,
  currentMode: String,
  isLoading: Boolean
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const inputRef = ref(null)

const modeLabels = {
  'simple': '简单聊天',
  'memory': '记忆聊天',
  'stream': '流式聊天',
  'rag': 'RAG 问答',
  'tool': '工具调用',
  'mcp': 'MCP 工具'
}

const getModeLabel = (mode) => modeLabels[mode] || mode

const handleSend = () => {
  const message = inputMessage.value.trim()
  if (message && !props.isLoading) {
    emit('send-message', message)
    inputMessage.value = ''
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.chat-container {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 20px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.mode-indicator {
  font-size: 14px;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 16px;
  border-radius: 20px;
}

.mode-indicator span {
  font-weight: 600;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-message {
  max-width: 600px;
  margin: 50px auto;
  text-align: center;
  color: #666;
}

.welcome-message h2 {
  color: #667eea;
  margin-bottom: 20px;
}

.welcome-message p {
  margin-bottom: 20px;
  line-height: 1.6;
}

.welcome-message ul {
  text-align: left;
  list-style: none;
  padding: 0;
}

.welcome-message li {
  padding: 10px;
  margin-bottom: 8px;
  background: #f5f5f5;
  border-radius: 6px;
  line-height: 1.6;
}

.input-area {
  padding: 20px 30px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 12px;
  background: #fafafa;
}

textarea {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  transition: border-color 0.3s ease;
}

textarea:focus {
  outline: none;
  border-color: #667eea;
}

.send-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:active {
  transform: translateY(0);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.loading {
  display: inline-block;
  width: 16px;
  height: 16px;
}

.loading::after {
  content: '';
  display: block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #fff;
  border-color: #fff transparent #fff transparent;
  animation: loading 1.2s linear infinite;
}

@keyframes loading {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .chat-header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }

  .messages {
    padding: 20px 15px;
  }

  .input-area {
    padding: 15px;
    flex-direction: column;
  }

  .send-btn {
    width: 100%;
  }
}

::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
