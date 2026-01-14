import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

export const simpleChat = async (params) => {
  const { data } = await api.post('/chat/simple', {
    message: params.message,
    temperature: params.temperature,
    maxTokens: params.maxTokens
  })
  return data
}

export const memoryChat = async (params) => {
  const { data } = await api.post('/chat/memory', {
    message: params.message,
    sessionId: params.sessionId,
    temperature: params.temperature,
    maxTokens: params.maxTokens
  })
  return data
}

export const streamChat = async (params, onToken) => {
  const response = await fetch('/api/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      message: params.message,
      sessionId: params.sessionId,
      temperature: params.temperature,
      maxTokens: params.maxTokens
    })
  })

  if (!response.ok) {
    throw new Error('Stream request failed')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let currentEvent = null

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()

    for (const line of lines) {
      // 解析 event 类型
      if (line.startsWith('event:')) {
        currentEvent = line.substring(6).trim()
      }
      // 解析 data 内容
      else if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (data && currentEvent === 'token') {
          // 只处理 token 事件，逐字显示
          onToken(data)
        }
        // complete 事件会在前端自动处理，这里不需要特别处理
      }
    }
  }
}

export const ragChat = async (params) => {
  const { data } = await api.post('/rag/query', {
    query: params.message,
    topK: 3
  })
  return data
}

export const toolChat = async (params) => {
  const { data } = await api.post('/tool/chat', {
    message: params.message,
    temperature: params.temperature
  })
  return data
}

export const mcpChat = async (params) => {
  const { data } = await api.post('/mcp/chat', {
    message: params.message,
    temperature: params.temperature
  })
  return data
}

export const getMcpTools = async () => {
  const { data } = await api.get('/mcp/tools')
  return data
}

export const getLocalTools = async () => {
  const { data } = await api.get('/tool/available')
  return data
}

// AI Assistant Service API
export const assistantChat = async (params) => {
  const { data } = await api.post('/assistant/chat', {
    message: params.message
  })
  return data
}

export const assistantChatCustom = async (params) => {
  const { data } = await api.post('/assistant/chat-custom', {
    systemMessage: params.systemMessage,
    message: params.message
  })
  return data
}

export const assistantChatVariables = async (params) => {
  const { data } = await api.post('/assistant/chat-variables', {
    language: params.language,
    topic: params.topic
  })
  return data
}
