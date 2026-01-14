<template>
  <div class="assistant-input">
    <div class="mode-selector">
      <button
        v-for="mode in assistantModes"
        :key="mode.value"
        :class="['mode-btn', { active: selectedMode === mode.value }]"
        @click="selectedMode = mode.value"
      >
        {{ mode.label }}
      </button>
    </div>

    <!-- 简单助手聊天 -->
    <div v-if="selectedMode === 'simple'" class="input-group">
      <label>消息</label>
      <textarea
        v-model="simpleMessage"
        placeholder="输入你的问题..."
        rows="3"
        @keydown.enter.exact.prevent="handleSimpleSend"
      ></textarea>
      <button @click="handleSimpleSend" :disabled="isLoading || !simpleMessage.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 自定义系统提示词 -->
    <div v-if="selectedMode === 'custom'" class="input-group">
      <label>系统提示词</label>
      <textarea
        v-model="systemMessage"
        placeholder="例如: You are a professional translator."
        rows="2"
      ></textarea>

      <label>用户消息</label>
      <textarea
        v-model="customMessage"
        placeholder="输入你的问题..."
        rows="3"
        @keydown.enter.exact.prevent="handleCustomSend"
      ></textarea>
      <button @click="handleCustomSend" :disabled="isLoading || !customMessage.trim() || !systemMessage.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 变量模板 -->
    <div v-if="selectedMode === 'variables'" class="input-group">
      <label>语言 (Language)</label>
      <input
        v-model="language"
        type="text"
        placeholder="例如: Chinese, English, Japanese"
      />

      <label>话题 (Topic)</label>
      <input
        v-model="topic"
        type="text"
        placeholder="例如: artificial intelligence, cooking, history"
        @keydown.enter.exact.prevent="handleVariablesSend"
      />
      <button @click="handleVariablesSend" :disabled="isLoading || !language.trim() || !topic.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 简单助手聊天 (带工具) -->
    <div v-if="selectedMode === 'simple-tools'" class="input-group">
      <label>消息 <span class="tools-badge">支持工具调用</span></label>
      <textarea
        v-model="simpleMessage"
        placeholder="输入你的问题（可使用计算器、日期时间、文本处理工具）..."
        rows="3"
        @keydown.enter.exact.prevent="handleSimpleToolsSend"
      ></textarea>
      <button @click="handleSimpleToolsSend" :disabled="isLoading || !simpleMessage.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 自定义系统提示词 (带工具) -->
    <div v-if="selectedMode === 'custom-tools'" class="input-group">
      <label>系统提示词 <span class="tools-badge">支持工具调用</span></label>
      <textarea
        v-model="systemMessage"
        placeholder="例如: You are a professional translator."
        rows="2"
      ></textarea>

      <label>用户消息</label>
      <textarea
        v-model="customMessage"
        placeholder="输入你的问题..."
        rows="3"
        @keydown.enter.exact.prevent="handleCustomToolsSend"
      ></textarea>
      <button @click="handleCustomToolsSend" :disabled="isLoading || !customMessage.trim() || !systemMessage.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <!-- 变量模板 (带工具) -->
    <div v-if="selectedMode === 'variables-tools'" class="input-group">
      <label>语言 (Language) <span class="tools-badge">支持工具调用</span></label>
      <input
        v-model="language"
        type="text"
        placeholder="例如: Chinese, English, Japanese"
      />

      <label>话题 (Topic)</label>
      <input
        v-model="topic"
        type="text"
        placeholder="例如: artificial intelligence, cooking, history"
        @keydown.enter.exact.prevent="handleVariablesToolsSend"
      />
      <button @click="handleVariablesToolsSend" :disabled="isLoading || !language.trim() || !topic.trim()" class="send-btn">
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  isLoading: Boolean
})

const emit = defineEmits(['send-assistant-message'])

const selectedMode = ref('simple')
const simpleMessage = ref('')
const systemMessage = ref('')
const customMessage = ref('')
const language = ref('')
const topic = ref('')

const assistantModes = [
  { value: 'simple', label: '简单助手' },
  { value: 'custom', label: '自定义提示词' },
  { value: 'variables', label: '变量模板' },
  { value: 'simple-tools', label: '简单助手(工具)' },
  { value: 'custom-tools', label: '自定义(工具)' },
  { value: 'variables-tools', label: '变量(工具)' }
]

const handleSimpleSend = () => {
  if (!simpleMessage.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'simple',
    message: simpleMessage.value.trim()
  })
  simpleMessage.value = ''
}

const handleCustomSend = () => {
  if (!customMessage.value.trim() || !systemMessage.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'custom',
    systemMessage: systemMessage.value.trim(),
    message: customMessage.value.trim()
  })
  customMessage.value = ''
}

const handleVariablesSend = () => {
  if (!language.value.trim() || !topic.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'variables',
    language: language.value.trim(),
    topic: topic.value.trim()
  })
  language.value = ''
  topic.value = ''
}

const handleSimpleToolsSend = () => {
  if (!simpleMessage.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'simple-tools',
    message: simpleMessage.value.trim()
  })
  simpleMessage.value = ''
}

const handleCustomToolsSend = () => {
  if (!customMessage.value.trim() || !systemMessage.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'custom-tools',
    systemMessage: systemMessage.value.trim(),
    message: customMessage.value.trim()
  })
  customMessage.value = ''
}

const handleVariablesToolsSend = () => {
  if (!language.value.trim() || !topic.value.trim() || props.isLoading) return

  emit('send-assistant-message', {
    type: 'variables-tools',
    language: language.value.trim(),
    topic: topic.value.trim()
  })
  language.value = ''
  topic.value = ''
}
</script>

<style scoped>
.assistant-input {
  padding: 20px 30px;
  border-top: 1px solid #e0e0e0;
  background: #fafafa;
}

.mode-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.mode-btn {
  padding: 8px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
}

.mode-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group label {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.input-group textarea,
.input-group input {
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  transition: border-color 0.3s ease;
}

.input-group textarea:focus,
.input-group input:focus {
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
  align-self: flex-end;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.tools-badge {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .assistant-input {
    padding: 15px;
  }

  .mode-selector {
    flex-direction: column;
  }

  .mode-btn {
    width: 100%;
  }

  .send-btn {
    width: 100%;
  }
}
</style>
