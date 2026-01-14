<template>
  <div class="sidebar">
    <h2>聊天模式</h2>
    <div class="mode-buttons">
      <button
        v-for="mode in modes"
        :key="mode.value"
        :class="['mode-btn', { active: currentMode === mode.value }]"
        @click="$emit('mode-change', mode.value)"
      >
        {{ mode.label }}
      </button>
    </div>

    <div class="settings">
      <h3>设置</h3>

      <div class="setting-item">
        <label for="sessionId">会话ID</label>
        <input
          id="sessionId"
          type="text"
          :value="sessionId"
          @input="updateSettings({ sessionId: $event.target.value })"
          placeholder="user-123"
        />
      </div>

      <div class="setting-item">
        <label for="temperature">Temperature</label>
        <div style="display: flex; align-items: center;">
          <input
            id="temperature"
            type="range"
            :value="temperature"
            @input="updateSettings({ temperature: parseFloat($event.target.value) })"
            min="0"
            max="2"
            step="0.1"
            style="flex: 1; margin-right: 8px;"
          />
          <span class="value-display">{{ temperature }}</span>
        </div>
      </div>

      <div class="setting-item">
        <label for="maxTokens">Max Tokens</label>
        <input
          id="maxTokens"
          type="number"
          :value="maxTokens"
          @input="updateSettings({ maxTokens: parseInt($event.target.value) })"
          min="1"
          max="4096"
        />
      </div>

      <button class="clear-btn" @click="$emit('clear-chat')">
        清空对话
      </button>

      <button class="tools-btn" @click="$emit('show-local-tools')">
        查看本地工具
      </button>

      <button class="tools-btn" @click="$emit('show-tools')">
        查看MCP工具
      </button>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  currentMode: String,
  sessionId: String,
  temperature: Number,
  maxTokens: Number
})

const emit = defineEmits(['mode-change', 'settings-change', 'clear-chat', 'show-local-tools', 'show-tools'])

const modes = [
  { value: 'simple', label: '简单聊天' },
  { value: 'memory', label: '记忆聊天' },
  { value: 'stream', label: '流式聊天' },
  { value: 'assistant', label: 'AI 助手' },
  { value: 'rag', label: 'RAG 问答' },
  { value: 'tool', label: '工具调用' },
  { value: 'mcp', label: 'MCP 工具' }
]

const updateSettings = (newSettings) => {
  emit('settings-change', {
    sessionId: props.sessionId,
    temperature: props.temperature,
    maxTokens: props.maxTokens,
    ...newSettings
  })
}
</script>

<style scoped>
.sidebar {
  width: 300px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.sidebar h3 {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.mode-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mode-btn {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s ease;
}

.mode-btn:hover {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.settings {
  border-top: 1px solid #e0e0e0;
  padding-top: 20px;
}

.setting-item {
  margin-bottom: 15px;
}

.setting-item label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}

.setting-item input[type="text"],
.setting-item input[type="number"] {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
}

.value-display {
  font-size: 12px;
  color: #667eea;
  font-weight: 600;
  min-width: 30px;
}

.clear-btn,
.tools-btn {
  width: 100%;
  padding: 10px;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s ease;
  margin-bottom: 8px;
}

.clear-btn:hover {
  background: #ff4444;
  color: white;
  border-color: #ff4444;
}

.tools-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 100%;
    height: auto;
    max-height: 300px;
  }

  .mode-buttons {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .mode-btn {
    flex: 1;
    min-width: 120px;
  }
}
</style>
