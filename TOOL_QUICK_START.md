# 工具调用接口快速入门

## 快速测试

### 1. 启动应用

```bash
cd langchain4j-demo-core
mvn spring-boot:run
```

应用启动后，监听在 `http://localhost:8080`

### 2. 测试基础计算

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 25 + 17",
    "enabledTools": ["add"]
  }'
```

**预期响应**:
```json
{
  "success": true,
  "content": "25 + 17 的结果是 42。",
  "toolExecutions": [
    {
      "toolName": "add",
      "arguments": "{\"a\":25.0,\"b\":17.0}",
      "result": "42.0"
    }
  ]
}
```

### 3. 测试多步骤计算

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "先计算 100 减 25，然后把结果乘以 3",
    "enabledTools": ["add", "subtract", "multiply", "divide"]
  }'
```

### 4. 测试天气查询

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "北京的天气怎么样？",
    "enabledTools": ["get_weather"]
  }'
```

### 5. 获取可用工具列表

```bash
curl http://localhost:8080/api/tool/available
```

## 使用 Postman 测试

### 步骤 1: 创建请求

- **Method**: POST
- **URL**: `http://localhost:8080/api/tool/chat`
- **Headers**: `Content-Type: application/json`

### 步骤 2: 设置请求体

```json
{
  "message": "计算 25 + 17",
  "enabledTools": ["add"]
}
```

### 步骤 3: 发送请求并查看响应

## 运行测试用例

### 运行所有测试

```bash
cd langchain4j-demo-core
mvn test -Dtest=ToolServiceTest
```

### 运行单个测试

```bash
# 测试加法工具
mvn test -Dtest=ToolServiceTest#testAddTool

# 测试多个工具
mvn test -Dtest=ToolServiceTest#testMultipleTools

# 测试天气工具
mvn test -Dtest=ToolServiceTest#testWeatherTool

# 测试时间工具
mvn test -Dtest=ToolServiceTest#testTimeTool

# 测试不需要工具的对话
mvn test -Dtest=ToolServiceTest#testNoToolNeeded

# 测试默认工具
mvn test -Dtest=ToolServiceTest#testDefaultTools

# 测试除以零
mvn test -Dtest=ToolServiceTest#testDivideByZero

# 测试复杂计算
mvn test -Dtest=ToolServiceTest#testComplexCalculation
```

## 快速示例集合

### 数学运算

```bash
# 加法
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "25 加 17", "enabledTools": ["add"]}'

# 减法
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "100 减 25", "enabledTools": ["subtract"]}'

# 乘法
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "75 乘以 3", "enabledTools": ["multiply"]}'

# 除法
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "225 除以 3", "enabledTools": ["divide"]}'
```

### 复杂计算

```bash
# 多步骤计算
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 (25 + 15) * 3 - 10",
    "enabledTools": ["add", "subtract", "multiply"]
  }'
```

### 信息查询

```bash
# 天气查询
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "北京和上海的天气如何？", "enabledTools": ["get_weather"]}'

# 时间查询
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？", "enabledTools": ["get_time"]}'
```

### 智能对话

```bash
# 不使用工具的对话
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下你自己",
    "enabledTools": ["add", "get_weather"]
  }'
```

## 前端集成示例

### JavaScript (Fetch API)

```javascript
async function chatWithTools(message, enabledTools) {
  const response = await fetch('http://localhost:8080/api/tool/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      message: message,
      enabledTools: enabledTools
    })
  });

  const data = await response.json();

  console.log('AI 回复:', data.content);
  console.log('工具调用次数:', data.toolExecutions?.length || 0);

  if (data.toolExecutions && data.toolExecutions.length > 0) {
    console.log('工具调用详情:');
    data.toolExecutions.forEach((execution, index) => {
      console.log(`${index + 1}. ${execution.toolName}`);
      console.log(`   参数: ${execution.arguments}`);
      console.log(`   结果: ${execution.result}`);
    });
  }

  return data;
}

// 使用示例
chatWithTools('计算 25 + 17', ['add']);
chatWithTools('北京的天气怎么样？', ['get_weather']);
```

### Vue.js 组件示例

```vue
<template>
  <div class="tool-chat">
    <div class="messages">
      <div v-for="msg in messages" :key="msg.id" class="message">
        <div class="role">{{ msg.role }}:</div>
        <div class="content">{{ msg.content }}</div>

        <div v-if="msg.toolExecutions && msg.toolExecutions.length > 0" class="tools">
          <div class="tool-title">工具调用:</div>
          <div v-for="(exec, idx) in msg.toolExecutions" :key="idx" class="tool-execution">
            <div>🔧 {{ exec.toolName }}</div>
            <div class="tool-args">参数: {{ exec.arguments }}</div>
            <div class="tool-result">结果: {{ exec.result }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <select v-model="selectedTools" multiple>
        <option value="add">加法</option>
        <option value="subtract">减法</option>
        <option value="multiply">乘法</option>
        <option value="divide">除法</option>
        <option value="get_weather">天气查询</option>
        <option value="get_time">时间查询</option>
      </select>

      <input v-model="userInput" @keyup.enter="sendMessage" />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      messages: [],
      userInput: '',
      selectedTools: ['add', 'subtract', 'multiply', 'divide']
    };
  },
  methods: {
    async sendMessage() {
      if (!this.userInput.trim()) return;

      // 添加用户消息
      this.messages.push({
        id: Date.now(),
        role: 'user',
        content: this.userInput
      });

      const userMessage = this.userInput;
      this.userInput = '';

      try {
        const response = await fetch('http://localhost:8080/api/tool/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            message: userMessage,
            enabledTools: this.selectedTools
          })
        });

        const data = await response.json();

        // 添加 AI 回复
        this.messages.push({
          id: Date.now(),
          role: 'assistant',
          content: data.content,
          toolExecutions: data.toolExecutions
        });
      } catch (error) {
        console.error('Error:', error);
        this.messages.push({
          id: Date.now(),
          role: 'system',
          content: '错误: ' + error.message
        });
      }
    }
  }
};
</script>

<style scoped>
.tool-chat {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.messages {
  height: 500px;
  overflow-y: auto;
  border: 1px solid #ddd;
  padding: 10px;
  margin-bottom: 20px;
}

.message {
  margin-bottom: 15px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 5px;
}

.role {
  font-weight: bold;
  margin-bottom: 5px;
}

.tools {
  margin-top: 10px;
  padding: 10px;
  background: #e8f4f8;
  border-radius: 5px;
}

.tool-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.tool-execution {
  margin: 5px 0;
  padding: 5px;
  background: white;
  border-radius: 3px;
  font-size: 0.9em;
}

.tool-args, .tool-result {
  margin-left: 20px;
  color: #666;
}

.input-area {
  display: flex;
  gap: 10px;
}

.input-area select {
  width: 200px;
}

.input-area input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.input-area button {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.input-area button:hover {
  background: #0056b3;
}
</style>
```

## 常见问题

### Q1: 如何添加自定义工具？

参考 `TOOL_CALLING_API.md` 的"扩展新工具"章节。

### Q2: 为什么 AI 没有调用工具？

可能的原因：
1. AI 认为不需要工具就能回答
2. 工具描述不够清晰
3. 没有启用合适的工具

### Q3: 如何让 AI 更倾向于使用工具？

在消息中明确要求：
```json
{
  "message": "请使用工具计算 25 + 17",
  "enabledTools": ["add"]
}
```

### Q4: enabledTools 为空会怎样？

默认使用所有数学运算工具（add、subtract、multiply、divide）。

### Q5: 工具执行失败怎么办？

工具执行失败时，会返回错误信息给 AI，AI 会根据错误生成友好的回复。

## 下一步

- 查看 `TOOL_CALLING_API.md` 了解完整的 API 文档
- 查看 `TOOL_CALLING_COMPARISON.md` 了解 @Tool 注解和 ToolSpecification 的对比
- 查看 `ToolServiceTest.java` 了解更多测试示例
- 尝试添加自己的自定义工具

## 相关文档

- [完整 API 文档](./TOOL_CALLING_API.md)
- [工具调用方式对比](./TOOL_CALLING_COMPARISON.md)
- [MCP 协议详解](./MCP_PROTOCOL_EXPLAINED.md)
- [工具调用方法总览](./TOOL_CALLING_METHODS.md)
