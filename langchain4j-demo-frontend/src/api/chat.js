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

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (data) {
          onToken(data)
        }
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
