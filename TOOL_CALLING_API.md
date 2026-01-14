# 工具调用 API 文档

## 概述

本文档介绍如何使用 `/api/tool/chat` 接口进行动态工具调用。该接口使用 LangChain4j 的 `ToolSpecification` 实现，允许在运行时动态定义和调用工具，无需使用 `@Tool` 注解。

## 核心概念

### 工具调用流程

```
用户消息
   ↓
1. AI 分析消息，决定是否需要调用工具
   ↓
2. 如果需要，AI 选择合适的工具并生成参数
   ↓
3. 后端执行工具函数
   ↓
4. 将工具执行结果返回给 AI
   ↓
5. AI 根据工具结果生成最终回复
   ↓
返回给用户
```

### 两次 AI 调用

该实现包含两次 AI 调用：

1. **第一次调用**：AI 分析用户消息，决定调用哪些工具
2. **第二次调用**：AI 根据工具执行结果，生成用户友好的回复

## API 接口

### 1. 工具调用聊天

**接口**: `POST /api/tool/chat`

**请求体**:
```json
{
  "message": "用户消息",
  "enabledTools": ["tool1", "tool2", ...]
}
```

**响应体**:
```json
{
  "success": true,
  "content": "AI 的最终回复",
  "toolExecutions": [
    {
      "toolName": "add",
      "arguments": "{\"a\":25,\"b\":17}",
      "result": "42.0"
    }
  ],
  "errorMessage": null
}
```

### 2. 获取可用工具列表

**接口**: `GET /api/tool/available`

**响应体**:
```json
{
  "tools": [
    {
      "name": "add",
      "description": "计算两个数的和",
      "category": "数学运算"
    },
    {
      "name": "get_weather",
      "description": "获取指定城市的天气信息",
      "category": "信息查询"
    }
  ]
}
```

### 3. 测试单个工具

**接口**: `POST /api/tool/test`

与 `/api/tool/chat` 相同，用于测试特定工具。

## 可用工具列表

### 数学运算工具

#### 1. add - 加法
```json
{
  "name": "add",
  "description": "计算两个数的和",
  "parameters": {
    "a": "第一个数字",
    "b": "第二个数字"
  }
}
```

**示例**:
```json
{
  "message": "计算 25 + 17",
  "enabledTools": ["add"]
}
```

#### 2. subtract - 减法
```json
{
  "name": "subtract",
  "description": "计算两个数的差",
  "parameters": {
    "a": "被减数",
    "b": "减数"
  }
}
```

**示例**:
```json
{
  "message": "100 减去 25 等于多少",
  "enabledTools": ["subtract"]
}
```

#### 3. multiply - 乘法
```json
{
  "name": "multiply",
  "description": "计算两个数的乘积",
  "parameters": {
    "a": "第一个数字",
    "b": "第二个数字"
  }
}
```

**示例**:
```json
{
  "message": "75 乘以 3",
  "enabledTools": ["multiply"]
}
```

#### 4. divide - 除法
```json
{
  "name": "divide",
  "description": "计算两个数的商",
  "parameters": {
    "a": "被除数",
    "b": "除数"
  }
}
```

**示例**:
```json
{
  "message": "225 除以 3",
  "enabledTools": ["divide"]
}
```

**注意**: 除数为 0 时，返回错误信息。

### 信息查询工具

#### 5. get_weather - 天气查询
```json
{
  "name": "get_weather",
  "description": "获取指定城市的天气信息",
  "parameters": {
    "city": "城市名称"
  }
}
```

**示例**:
```json
{
  "message": "北京的天气怎么样？",
  "enabledTools": ["get_weather"]
}
```

**响应示例**:
```
北京的天气：晴，温度25°C，湿度60%，风速3m/s
```

#### 6. get_time - 时间查询
```json
{
  "name": "get_time",
  "description": "获取当前时间",
  "parameters": {}
}
```

**示例**:
```json
{
  "message": "现在几点了？",
  "enabledTools": ["get_time"]
}
```

## 使用示例

### 示例 1: 简单加法

**请求**:
```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 25 + 17 的结果",
    "enabledTools": ["add"]
  }'
```

**响应**:
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

### 示例 2: 多步骤计算

**请求**:
```json
{
  "message": "先计算 100 减 25，然后把结果乘以 3",
  "enabledTools": ["add", "subtract", "multiply", "divide"]
}
```

**响应**:
```json
{
  "success": true,
  "content": "首先，100 - 25 = 75，然后 75 × 3 = 225。",
  "toolExecutions": [
    {
      "toolName": "subtract",
      "arguments": "{\"a\":100.0,\"b\":25.0}",
      "result": "75.0"
    },
    {
      "toolName": "multiply",
      "arguments": "{\"a\":75.0,\"b\":3.0}",
      "result": "225.0"
    }
  ]
}
```

### 示例 3: 天气查询

**请求**:
```json
{
  "message": "北京和上海的天气分别怎么样？",
  "enabledTools": ["get_weather"]
}
```

**响应**:
```json
{
  "success": true,
  "content": "北京今天晴天，温度25°C，湿度60%，风速3m/s。上海今天也是晴天，温度25°C，湿度60%，风速3m/s。",
  "toolExecutions": [
    {
      "toolName": "get_weather",
      "arguments": "{\"city\":\"北京\"}",
      "result": "北京的天气：晴，温度25°C，湿度60%，风速3m/s"
    },
    {
      "toolName": "get_weather",
      "arguments": "{\"city\":\"上海\"}",
      "result": "上海的天气：晴，温度25°C，湿度60%，风速3m/s"
    }
  ]
}
```

### 示例 4: 不需要工具的对话

**请求**:
```json
{
  "message": "你好，请介绍一下你自己",
  "enabledTools": ["add", "get_weather"]
}
```

**响应**:
```json
{
  "success": true,
  "content": "你好！我是一个 AI 助手，可以帮助你进行计算、查询天气等任务。有什么我可以帮到你的吗？",
  "toolExecutions": []
}
```

**说明**: AI 判断用户的问题不需要调用工具，直接回复。

### 示例 5: 默认工具

**请求**:
```json
{
  "message": "计算 100 除以 4",
  "enabledTools": null
}
```

或者省略 `enabledTools` 字段。

**响应**:
```json
{
  "success": true,
  "content": "100 ÷ 4 = 25",
  "toolExecutions": [
    {
      "toolName": "divide",
      "arguments": "{\"a\":100.0,\"b\":4.0}",
      "result": "25.0"
    }
  ]
}
```

**说明**: 如果不指定工具列表，默认使用所有数学运算工具（add、subtract、multiply、divide）。

## 实现原理

### 1. ToolSpecification 动态工具定义

```java
ToolSpecification toolSpec = ToolSpecification.builder()
    .name("add")
    .description("计算两个数的和")
    .parameters(JsonObjectSchema.builder()
        .addNumberProperty("a", "第一个数字")
        .addNumberProperty("b", "第二个数字")
        .required("a", "b")
        .build())
    .build();
```

### 2. 第一次 AI 调用

```java
ChatRequest request = ChatRequest.builder()
    .messages(UserMessage.from(userMessage))
    .toolSpecifications(toolSpecs)  // 传入可用工具
    .build();

ChatResponse response = chatModel.chat(request);
```

AI 分析消息后，返回需要调用的工具和参数。

### 3. 执行工具

```java
for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
    String toolResult = executeToolFunction(
        toolRequest.name(),
        toolRequest.arguments()
    );

    messages.add(ToolExecutionResultMessage.from(toolRequest, toolResult));
}
```

### 4. 第二次 AI 调用

```java
ChatRequest finalRequest = ChatRequest.builder()
    .messages(messages)  // 包含用户消息、AI 消息、工具结果
    .toolSpecifications(toolSpecs)
    .build();

ChatResponse finalResponse = chatModel.chat(finalRequest);
```

AI 根据工具结果生成最终回复。

## 与 @Tool 注解的对比

| 特性 | @Tool 注解 | ToolSpecification |
|------|-----------|-------------------|
| **定义方式** | 编译时，使用注解 | 运行时，动态构建 |
| **灵活性** | 固定，需要重新编译 | 动态，可随时添加/修除 |
| **代码量** | 少，声明式 | 多，需要手动定义规范 |
| **工具执行** | 自动，AiServices 处理 | 手动，需要自己实现 |
| **适用场景** | 稳定的内置工具 | 动态配置的工具 |

### @Tool 注解示例

```java
public class Calculator {
    @Tool("计算两个数的和")
    public double add(@P("第一个数字") double a, @P("第二个数字") double b) {
        return a + b;
    }
}

// 使用
Assistant assistant = AiServices.builder(Assistant.class)
    .tools(new Calculator())
    .build();
```

### ToolSpecification 示例

```java
// 1. 定义工具规范
ToolSpecification spec = ToolSpecification.builder()
    .name("add")
    .description("计算两个数的和")
    .parameters(...)
    .build();

// 2. AI 调用
ChatRequest request = ChatRequest.builder()
    .messages(...)
    .toolSpecifications(spec)
    .build();

// 3. 手动执行工具
String result = executeToolFunction(name, args);

// 4. 返回结果给 AI
messages.add(ToolExecutionResultMessage.from(...));
```

## 错误处理

### 1. 工具执行失败

```json
{
  "success": true,
  "content": "抱歉，计算过程中出现了错误：除数不能为0",
  "toolExecutions": [
    {
      "toolName": "divide",
      "arguments": "{\"a\":10.0,\"b\":0.0}",
      "result": "错误：除数不能为0"
    }
  ]
}
```

工具返回错误信息，AI 会根据错误信息生成友好的回复。

### 2. 整体异常

```json
{
  "success": false,
  "content": null,
  "toolExecutions": [],
  "errorMessage": "Tool call error: ..."
}
```

当整个工具调用流程出现异常时，返回失败状态。

## 扩展新工具

### 步骤 1: 在 getToolSpecification 中添加工具定义

```java
case "new_tool" -> ToolSpecification.builder()
    .name("new_tool")
    .description("工具描述")
    .parameters(JsonObjectSchema.builder()
        .addStringProperty("param1", "参数1描述")
        .addNumberProperty("param2", "参数2描述")
        .required("param1", "param2")
        .build())
    .build();
```

### 步骤 2: 在 executeToolFunction 中添加工具执行逻辑

```java
case "new_tool" -> {
    String param1 = (String) args.get("param1");
    double param2 = getNumberFromMap(args, "param2");

    // 执行具体逻辑
    String result = doSomething(param1, param2);

    yield result;
}
```

### 步骤 3: 在 Controller 的 getAvailableTools 中添加工具信息

```java
new ToolInfo("new_tool", "工具描述", "工具分类")
```

## 最佳实践

### 1. 工具粒度

- ✅ 每个工具只做一件事（单一职责）
- ✅ 工具参数清晰明确
- ❌ 避免创建功能过于复杂的工具

### 2. 工具描述

- ✅ 描述要清晰、准确
- ✅ 参数说明要详细
- ❌ 避免模糊或歧义的描述

### 3. 错误处理

- ✅ 工具执行失败时返回友好的错误信息
- ✅ 捕获并处理所有异常
- ❌ 不要让异常直接抛出

### 4. 工具选择

- ✅ 只启用用户需要的工具
- ✅ 避免启用过多无关工具
- ❌ 不要让 AI 在太多工具中选择（会降低准确率）

## 性能考虑

### 工具调用开销

1. **第一次 AI 调用**: ~1-2 秒
2. **工具执行**: ~1-100 毫秒（取决于工具复杂度）
3. **第二次 AI 调用**: ~1-2 秒

总延迟: 约 2-5 秒

### 优化建议

1. **减少 AI 调用次数**: 如果不需要 AI 生成友好回复，可以只调用一次
2. **工具执行优化**: 对于耗时的工具（如网络请求），考虑添加缓存
3. **并行工具执行**: 如果多个工具互相独立，可以并行执行

## 总结

`/api/tool/chat` 接口提供了一个灵活的工具调用框架：

- ✅ 动态配置工具（无需重新编译）
- ✅ 支持多步骤工具调用
- ✅ 完整的错误处理
- ✅ 易于扩展新工具

适用场景：
- 需要动态添加/移除工具
- 工具需要根据用户配置启用
- 需要记录详细的工具调用过程
- 工具调用逻辑复杂，需要手动控制
