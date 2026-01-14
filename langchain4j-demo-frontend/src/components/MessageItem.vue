<template>
  <div :class="['message', message.role]">
    <div class="message-avatar">
      {{ message.role === 'user' ? 'U' : 'AI' }}
    </div>
    <div class="message-wrapper">
      <div :class="['message-content', { streaming: message.metadata.streaming }]">
        {{ message.content }}
      </div>
      <div v-if="hasMetadata" class="message-metadata">
        <!-- Token usage -->
        <div v-if="message.metadata.tokens">
          Tokens: {{ message.metadata.tokens.inputTokens || 0 }} 输入 /
          {{ message.metadata.tokens.outputTokens || 0 }} 输出
        </div>

        <!-- Model name -->
        <div v-if="message.metadata.model">
          Model: {{ message.metadata.model }}
        </div>

        <!-- Session ID -->
        <div v-if="message.metadata.sessionId">
          Session: {{ message.metadata.sessionId }}
        </div>

        <!-- Tool calls -->
        <div v-if="message.metadata.toolCalls && message.metadata.toolCalls.length > 0">
          <strong>工具调用:</strong>
          <div
            v-for="(call, index) in message.metadata.toolCalls"
            :key="index"
            class="tool-call"
          >
            <span class="tool-call-name">{{ call.toolName }}</span>:
            {{ call.result || JSON.stringify(call.arguments) }}
          </div>
          <!-- Tool call token usage -->
          <div v-if="message.metadata.tokens" class="tool-tokens">
            Token消耗: 输入 {{ message.metadata.tokens.inputTokens || 0 }} /
            输出 {{ message.metadata.tokens.outputTokens || 0 }} /
            总计 {{ message.metadata.tokens.totalTokens || 0 }}
          </div>
        </div>

        <!-- MCP tools -->
        <div v-if="message.metadata.mcpTools && message.metadata.mcpTools.length > 0">
          <strong>MCP工具:</strong> {{ message.metadata.mcpTools.join(', ') }}
        </div>

        <!-- Sources -->
        <div v-if="message.metadata.sources && message.metadata.sources.length > 0">
          <strong>来源:</strong> {{ message.metadata.sources.join(', ') }}
        </div>

        <!-- Error indicator -->
        <div v-if="message.metadata.error" style="color: #d32f2f;">
          发生错误
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, defineProps } from 'vue'

const props = defineProps({
  message: Object
})

const hasMetadata = computed(() => {
  const meta = props.message.metadata
  return meta && (
    meta.tokens ||
    meta.model ||
    meta.sessionId ||
    (meta.toolCalls && meta.toolCalls.length > 0) ||
    (meta.mcpTools && meta.mcpTools.length > 0) ||
    (meta.sources && meta.sources.length > 0) ||
    meta.error
  )
})
</script>

<style scoped>
.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-content.streaming::after {
  content: '▊';
  animation: blink 1s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.message-metadata {
  font-size: 12px;
  color: #666;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  max-width: 100%;
}

.message-metadata > div {
  margin-bottom: 4px;
}

.message-metadata > div:last-child {
  margin-bottom: 0;
}

.tool-call {
  background: #fff3cd;
  padding: 6px 10px;
  border-radius: 6px;
  margin-top: 6px;
  color: #856404;
}

.tool-call-name {
  font-weight: 600;
  color: #667eea;
}

.tool-tokens {
  background: #e3f2fd;
  padding: 6px 10px;
  border-radius: 6px;
  margin-top: 6px;
  color: #1565c0;
  font-weight: 500;
  font-size: 11px;
}

@media (max-width: 768px) {
  .message {
    max-width: 90%;
  }
}
</style>
