# MCP 实现指南

## 当前状态

✅ **已完成 MCP 功能实现**

- McpClientConfiguration: 已实现 HTTP/SSE 传输配置
- McpServiceImpl: 已实现完整的 MCP 聊天、工具列表和工具调用功能
- 编译状态: ✅ BUILD SUCCESS
- 待测试: 需要启动 MCP Server 进行集成测试

## MCP (Model Context Protocol) 简介

MCP 是一个允许 LLM 应用连接到外部工具和数据源的协议。它定义了:
- 客户端-服务器架构
- 工具调用规范
- 数据交换格式

## 架构设计

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   LLM (OpenAI)  │◄────►│  LangChain4j     │◄────►│   MCP Server    │
│                 │      │  McpClient       │      │   (Port 8081)   │
└─────────────────┘      └──────────────────┘      └─────────────────┘
                                                            │
                                                            ▼
                                                    ┌─────────────────┐
                                                    │   External      │
                                                    │   Tools/APIs    │
                                                    └─────────────────┘
```

## 实现步骤

### 1. MCP Client 配置

**文件**: `McpClientConfiguration.java`

需要配置:
- MCP Server 地址 (http://localhost:8081)
- 传输方式 (HTTP/SSE)
- 连接初始化

### 2. McpServiceImpl 实现

**核心方法**:

#### a) chatWithMcp()
```
流程:
1. 获取 MCP Server 提供的工具列表
2. 将工具转换为 LangChain4j ToolSpecification
3. 发送用户消息到 LLM（携带可用工具列表）
4. LLM 决定是否调用工具
5. 如果调用，执行 MCP 工具并获取结果
6. 将结果返回给 LLM 生成最终回复
```

#### b) listAvailableTools()
```
功能: 获取 MCP Server 提供的所有工具
返回: 工具名称、描述、参数schema
```

#### c) invokeTool()
```
功能: 直接调用指定的 MCP 工具
参数: 工具名称、参数Map
返回: 工具执行结果
```

## 实现详情

### 1. McpClientConfiguration

实现了完整的 MCP Client 配置:

```java
@Bean
public McpClient mcpClient() {
    // 配置 HTTP/SSE 传输
    HttpMcpTransport transport = new HttpMcpTransport.Builder()
            .sseUrl(mcpServerSseUrl)  // http://localhost:8081/sse
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(true)
            .logResponses(true)
            .build();

    // 构建 MCP Client
    McpClient client = new DefaultMcpClient.Builder()
            .transport(transport)
            .clientName("langchain4j-demo")
            .clientVersion("1.0.0")
            .toolExecutionTimeout(Duration.ofSeconds(30))
            .build();

    // 健康检查
    client.checkHealth();

    return client;
}
```

### 2. McpServiceImpl 实现

#### chatWithMcp() - 完整的工具调用流程

```java
public McpResponse chatWithMcp(McpRequest request) {
    // 1. 获取工具列表
    List<ToolSpecification> tools = mcpClient.listTools();

    // 2. 发送消息到 LLM（携带工具列表）
    ChatRequest chatRequest = ChatRequest.builder()
            .messages(UserMessage.from(request.getMessage()))
            .toolSpecifications(tools)
            .build();
    ChatResponse chatResponse = chatModel.chat(chatRequest);

    // 3. 检查是否需要调用工具
    if (!aiMessage.hasToolExecutionRequests()) {
        return McpResponse.builder()
                .content(aiMessage.text())
                .build();
    }

    // 4. 执行工具调用
    for (ToolExecutionRequest toolRequest : toolExecutionRequests) {
        String toolResult = mcpClient.executeTool(toolRequest);
        messages.add(ToolExecutionResultMessage.from(toolRequest, toolResult));
    }

    // 5. 生成最终回复
    ChatResponse finalResponse = chatModel.chat(finalRequest);
    return McpResponse.builder()
            .content(finalResponse.aiMessage().text())
            .toolsUsed(toolNames)
            .build();
}
```

#### listAvailableTools() - 工具列表

```java
public List<Map<String, Object>> listAvailableTools() {
    List<ToolSpecification> tools = mcpClient.listTools();

    return tools.stream()
            .map(tool -> {
                Map<String, Object> toolMap = new HashMap<>();
                toolMap.put("name", tool.name());
                toolMap.put("description", tool.description());
                toolMap.put("parameters", tool.parameters());
                return toolMap;
            })
            .collect(Collectors.toList());
}
```

#### invokeTool() - 直接工具调用

```java
public String invokeTool(String toolName, Map<String, Object> parameters) {
    // 转换参数为 JSON
    String argumentsJson = objectMapper.writeValueAsString(parameters);

    // 构建请求
    ToolExecutionRequest request = ToolExecutionRequest.builder()
            .name(toolName)
            .arguments(argumentsJson)
            .build();

    // 执行工具
    return mcpClient.executeTool(request);
}
```

## API 兼容性说明

✅ **已解决所有 API 兼容性问题**

LangChain4j 1.0.0-beta3 的实际 API:

1. `McpClient.listTools()` → 返回 `List<ToolSpecification>`
2. `McpClient.executeTool(ToolExecutionRequest)` → 返回 `String`
3. `HttpMcpTransport.Builder.sseUrl(String)` → 配置 SSE URL
4. `McpClient.checkHealth()` → 健康检查方法
5. 无需手动 initialize()，客户端自动初始化

## 测试 MCP 功能

### 前提条件

1. **启动 MCP Server**
   ```bash
   cd mcp-server
   mvn spring-boot:run
   ```

2. **验证 MCP Server**
   ```bash
   curl http://localhost:8081/health
   curl http://localhost:8081/sse
   ```

### 测试步骤

1. **列出可用工具**
   ```bash
   curl http://localhost:8080/api/mcp/tools
   ```

2. **使用 MCP 工具聊天**
   ```bash
   curl -X POST http://localhost:8080/api/mcp/chat \
     -H "Content-Type: application/json" \
     -d '{
       "message": "请帮我查询今天的天气"
     }'
   ```

3. **直接调用工具**
   ```bash
   curl -X POST http://localhost:8080/api/mcp/invoke \
     -H "Content-Type: application/json" \
     -d '{
       "toolName": "get_weather",
       "parameters": {
         "city": "北京"
       }
     }'
   ```

## 示例工具

MCP Server 可以提供各种工具:

### 1. 文件系统工具
```
- list_files: 列出目录文件
- read_file: 读取文件内容
- write_file: 写入文件
```

### 2. API 调用工具
```
- get_weather: 获取天气信息
- search_web: 网页搜索
- translate: 文本翻译
```

### 3. 数据库工具
```
- query_db: 执行数据库查询
- insert_data: 插入数据
```

## 前端集成

前端已有 MCP 相关组件:
- `McpToolsModal.vue`: 显示可用工具列表
- MCP 聊天模式: 支持与 MCP 工具交互

## 调试技巧

### 1. 查看日志
```bash
# MCP Client 日志
2026-01-21 15:00:00 - Initializing MCP Client with server URL: http://localhost:8081
2026-01-21 15:00:01 - MCP Client initialized successfully
2026-01-21 15:00:02 - Found 5 MCP tools

# 工具调用日志
2026-01-21 15:00:10 - Executing MCP tool: get_weather with arguments: {"city":"北京"}
2026-01-21 15:00:11 - Tool execution result: 北京今天晴，温度15-25度
```

### 2. 常见问题

**问题 1: 无法连接到 MCP Server**
```
错误: Failed to initialize MCP Client
解决:
1. 确认 MCP Server 已启动
2. 检查端口 8081 是否被占用
3. 验证 application.yml 中的配置
```

**问题 2: 工具列表为空**
```
错误: Found 0 MCP tools
解决:
1. 检查 MCP Server 的工具注册
2. 查看 MCP Server 日志
3. 验证 MCP 协议版本兼容性
```

**问题 3: 工具调用失败**
```
错误: Failed to execute MCP tool
解决:
1. 检查工具参数格式
2. 验证工具权限
3. 查看工具实现的错误日志
```

## 配置参数

### application.yml
```yaml
mcp:
  server:
    base-url: http://localhost:8081
    sse-endpoint: /sse
    timeout: 60s
    retry:
      max-attempts: 3
      delay: 1000
```

## 后续工作

1. ✅ 创建 McpServiceImpl 框架
2. ✅ 配置 McpClient Bean
3. ✅ 实现 MCP 聊天功能（chatWithMcp）
4. ✅ 实现工具列表功能（listAvailableTools）
5. ✅ 实现工具调用功能（invokeTool）
6. ✅ 添加错误处理
7. ⏳ 启动 MCP Server
8. ⏳ 编写集成测试
9. ✅ 完善文档

## 参考资料

- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [LangChain4j MCP Documentation](https://docs.langchain4j.dev/integrations/mcp)
- 项目 MCP Server 代码: `/mcp-server/`

## 总结

MCP 功能的实现依赖于:
1. MCP Server 正常运行
2. LangChain4j MCP API 稳定
3. 正确的协议实现

当前已完成基础框架，待条件具备后可快速完善实现。
