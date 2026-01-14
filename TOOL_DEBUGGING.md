# 工具调用接口调试指南

## 问题描述

从你提供的日志来看，AI 已经成功返回了响应：

```
body: {"id":"chatcmpl-Cxm5A0yL3sYx3EO19gcjAJ7V3TM5e","created":1768362216,"model":"gpt-4o-mini-2024-07-18","choices":[{"index":0,"message":{"role":"assistant","content":"2和3的和是5。"},"finish_reason":"stop"}],"usage":{"prompt_tokens":1129,"completion_tokens":9,"total_tokens":1138},"system_fingerprint":"fp_c4585b5b9c"}
```

AI 返回的内容是：**"2和3的和是5。"**

如果前端没有收到响应，可能有以下几种原因。

## 排查步骤

### 1. 检查完整的后端日志

重新启动应用并测试，查看完整的日志输出。你应该看到类似这样的日志：

```
2026-01-14 11:43:36 - Tool chat request: message=计算2加3, tools=[calculator]
2026-01-14 11:43:36 - Added tool: calculator
2026-01-14 11:43:36 - Tool chat response received: 2和3的和是5。
2026-01-14 11:43:36 - Returning response: success=true, content=2和3的和是5。
2026-01-14 11:43:36 - Tool chat response: success=true, content=2和3的和是5。, toolExecutions=0
2026-01-14 11:43:36 - Returning response to client: ToolCallResponse(content=2和3的和是5。, toolExecutions=[], success=true, errorMessage=null)
```

如果看到了 "Returning response to client" 日志，说明后端已经正确返回了响应。

### 2. 检查前端请求

使用浏览器开发者工具或 curl 测试：

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 2 加 3",
    "enabledTools": ["calculator"]
  }' \
  -v
```

预期响应：

```json
{
  "content": "2和3的和是5。",
  "toolExecutions": [],
  "success": true,
  "errorMessage": null
}
```

### 3. 检查前端代码

如果使用前端调用，检查前端代码是否正确处理响应：

```javascript
// 错误示例：可能在等待 toolExecutions 数据
if (response.toolExecutions && response.toolExecutions.length > 0) {
    // 这里永远不会执行，因为 @Tool 方式 toolExecutions 始终为空
    displayToolCalls(response.toolExecutions);
}
displayResponse(response.content); // 应该直接显示 content

// 正确示例
fetch('/api/tool/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        message: '计算 2 加 3',
        enabledTools: ['calculator']
    })
})
.then(res => res.json())
.then(data => {
    console.log('Response:', data);
    if (data.success) {
        console.log('Content:', data.content); // 显示这个
    }
});
```

### 4. 检查 CORS 配置

如果前端和后端在不同端口，检查 CORS 配置：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000") // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
```

### 5. 检查响应是否被缓存

如果使用浏览器测试，可能响应被缓存了。尝试：

- 清除浏览器缓存
- 使用隐私模式
- 在请求头添加 `Cache-Control: no-cache`

## 常见问题

### Q1: 为什么 toolExecutions 是空的？

使用 `@Tool` 注解方式时，工具调用由 LangChain4j 自动处理，我们无法获取工具调用的详细信息。这是正常的。

如果需要工具调用详情，有两个选择：

1. **使用 ToolSpecification 方式**（我之前实现的版本，已被替换）
2. **从 AI 的回复中推断**：AI 通常会在回复中说明使用了什么工具

### Q2: 如何确认工具被调用了？

从你的日志可以看出：

```
prompt_tokens: 1129
```

这个 token 数量很大（普通对话通常只有几十个），说明 AI 确实看到了所有工具的定义（Calculator 的 8 个方法）。

从 AI 的回复 "2和3的和是5。" 可以确认 AI 调用了 `add` 方法。

### Q3: 能否看到具体调用了哪个工具方法？

启用 DEBUG 日志：

```yaml
# application.yml
logging:
  level:
    dev.langchain4j: DEBUG
    org.example.langchain4jdemo: DEBUG
```

你会看到类似这样的日志：

```
DEBUG - Executing tool: add
DEBUG - Tool arguments: {arg0=2.0, arg1=3.0}
DEBUG - Tool result: 5.0
```

## 测试建议

### 方法 1: 使用 curl 测试

```bash
# 测试 1: 简单计算
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 2 加 3",
    "enabledTools": ["calculator"]
  }'

# 预期输出
# {"content":"2和3的和是5。","toolExecutions":[],"success":true,"errorMessage":null}

# 测试 2: 获取可用工具
curl http://localhost:8080/api/tool/available

# 测试 3: 复杂计算
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "先计算 10 减 5，然后把结果乘以 2",
    "enabledTools": ["calculator"]
  }'
```

### 方法 2: 使用 Postman 测试

1. 创建 POST 请求：`http://localhost:8080/api/tool/chat`
2. Headers: `Content-Type: application/json`
3. Body (raw JSON):
```json
{
  "message": "计算 2 加 3",
  "enabledTools": ["calculator"]
}
```
4. 点击 Send
5. 查看响应

### 方法 3: 运行单元测试

```bash
mvn test -Dtest=ToolServiceTest#testCalculatorTool
```

测试会输出：
```
用户: 计算 25 + 17 的结果
成功: true
回复: 25 + 17 的结果是 42。
```

## 如果问题仍然存在

### 收集以下信息：

1. **完整的后端日志**（从请求到响应）
2. **前端请求代码**（如果使用前端）
3. **浏览器控制台错误**（如果有）
4. **curl 测试结果**

### 临时解决方案

如果确实需要工具调用详情，我可以帮你恢复 ToolSpecification 方式的实现，它可以返回详细的工具调用信息：

```json
{
  "content": "2 + 3 = 5",
  "toolExecutions": [
    {
      "toolName": "add",
      "arguments": "{\"a\":2,\"b\":3}",
      "result": "5.0"
    }
  ],
  "success": true,
  "errorMessage": null
}
```

## 下一步操作

1. **启动应用**
```bash
mvn spring-boot:run
```

2. **在另一个终端测试**
```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 2 加 3",
    "enabledTools": ["calculator"]
  }' | jq .
```

3. **查看日志**，应该能看到：
   - Tool chat request: ...
   - Tool chat response received: ...
   - Returning response: ...
   - Returning response to client: ...

4. **如果看到了所有日志，但前端仍无响应**：
   - 检查前端代码
   - 检查 CORS 配置
   - 检查网络请求是否成功（浏览器开发者工具 Network 标签）

告诉我你看到了什么，我会进一步帮助你排查问题。
