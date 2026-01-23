# MCP Client 使用指南

## 概述

本指南说明如何使用 **demo-core** 模块通过 MCP 协议远程调用 **mcp-server** 模块提供的工具。

这是两个独立的服务，通过标准的 MCP (Model Context Protocol) 协议进行通信。

## 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      demo-core (8080)                       │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │          McpController (REST API)                  │    │
│  │  - GET  /api/mcp/tools      获取工具列表          │    │
│  │  - POST /api/mcp/invoke     直接调用工具          │    │
│  │  - POST /api/mcp/chat       AI聊天(自动调工具)    │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                     │
│  ┌────────────────────▼───────────────────────────────┐    │
│  │            McpServiceImpl                          │    │
│  │  - chatWithMcp()         AI自动调用工具           │    │
│  │  - listAvailableTools()  列出远程工具             │    │
│  │  - invokeTool()          直接调用远程工具         │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                     │
│  ┌────────────────────▼───────────────────────────────┐    │
│  │      LangChain4j MCP Client                        │    │
│  │  - HTTP/SSE Transport                              │    │
│  │  - Stdio Transport                                 │    │
│  └────────────────────┬───────────────────────────────┘    │
└─────────────────────────┼─────────────────────────────────┘
                          │
                          │ MCP Protocol (HTTP/SSE or Stdio)
                          │
┌─────────────────────────▼─────────────────────────────────┐
│                    mcp-server (8081)                       │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │     Spring AI MCP Server WebMVC                    │    │
│  │  - MCP Protocol Handler                            │    │
│  │  - Tool Registration                               │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                     │
│       ┌───────────────┴───────────────┐                    │
│       │                               │                    │
│  ┌────▼─────────┐           ┌────────▼────────┐           │
│  │ Calculator   │           │  Weather Tool   │           │
│  │    Tool      │           │                 │           │
│  └──────────────┘           └─────────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

## 核心功能

### 1. 获取工具列表

通过 MCP 协议从远程 mcp-server 获取所有可用工具。

**API 端点**:
```
GET /api/mcp/tools
```

**实现**:
```java
@GetMapping("/tools")
public List<Map<String, Object>> listTools() {
    return mcpService.listAvailableTools();
}
```

**工作流程**:
1. demo-core 的 McpController 接收请求
2. McpServiceImpl 通过 LangChain4j MCP Client 发送 `tools/list` 请求
3. mcp-server 返回工具列表
4. 解析并返回给客户端

### 2. 直接调用工具

通过 MCP 协议直接调用远程 mcp-server 的指定工具。

**API 端点**:
```
POST /api/mcp/invoke?toolName={工具名}
Content-Type: application/json

{参数JSON}
```

**实现**:
```java
@PostMapping("/invoke")
public String invokeTool(
        @RequestParam String toolName,
        @RequestBody Map<String, Object> parameters) {
    return mcpService.invokeTool(toolName, parameters);
}
```

**工作流程**:
1. demo-core 接收工具调用请求
2. 验证工具是否存在（调用 `listTools()`）
3. 构建 `ToolExecutionRequest`
4. 通过 MCP 协议远程调用 mcp-server 的工具
5. 返回工具执行结果

**核心实现代码**:
```java
public String invokeTool(String toolName, Map<String, Object> parameters) {
    // 1. 验证工具
    List<ToolSpecification> toolSpecs = mcpClient.listTools();
    ToolSpecification toolSpec = toolSpecs.stream()
            .filter(spec -> spec.name().equals(toolName))
            .findFirst()
            .orElseThrow();

    // 2. 构建请求
    ToolExecutionRequest executionRequest = ToolExecutionRequest.builder()
            .id(UUID.randomUUID().toString())
            .name(toolName)
            .arguments(convertParametersToJson(parameters))
            .build();

    // 3. 创建工具提供者并执行（通过 MCP 协议远程调用）
    McpToolProvider toolProvider = McpToolProvider.builder()
            .mcpClients(mcpClient)
            .build();

    return toolProvider.execute(executionRequest, null);
}
```

### 3. AI 聊天（自动调用工具）

AI 根据用户问题自动通过 MCP 协议调用远程工具。

**API 端点**:
```
POST /api/mcp/chat
Content-Type: application/json

{
  "message": "用户问题"
}
```

**实现**:
```java
@PostMapping("/chat")
public McpResponse chatWithMcp(@RequestBody McpRequest request) {
    return mcpService.chatWithMcp(request);
}
```

**工作流程**:
1. 用户发送自然语言问题
2. McpServiceImpl 创建 AI Assistant（集成 MCP 工具）
3. AI 分析问题，自动通过 MCP 协议调用远程工具
4. AI 综合工具结果生成回答

## 传输方式

### 1. HTTP/SSE 模式（推荐用于独立服务）

**配置** (application.yml):
```yaml
mcp:
  transport:
    type: http
  server:
    url: http://localhost:8081
```

**特点**:
- ✅ 两个独立的服务
- ✅ 通过 HTTP/SSE 通信
- ✅ 需要手动启动 mcp-server
- ✅ 适合生产环境

**启动流程**:
```bash
# 1. 启动 mcp-server
cd mcp-server
mvn spring-boot:run

# 2. 启动 demo-core
cd langchain4j-demo-core
mvn spring-boot:run
```

### 2. Stdio 模式（适合开发测试）

**配置** (application.yml):
```yaml
mcp:
  transport:
    type: stdio
  server:
    jar: ../mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar
```

**特点**:
- ✅ demo-core 自动启动 mcp-server
- ✅ 通过标准输入输出通信
- ✅ 只需启动 demo-core
- ✅ 适合开发测试

**启动流程**:
```bash
# 只需启动 demo-core（会自动启动 mcp-server）
cd langchain4j-demo-core
mvn spring-boot:run
```

## 使用示例

### 示例 1: 获取工具列表

**请求**:
```bash
curl -X GET http://localhost:8080/api/mcp/tools
```

**响应**:
```json
[
  {
    "name": "calculator_add",
    "description": "A tool for adding two numbers together...",
    "parameters": "{type=object, properties={...}}"
  },
  {
    "name": "weather_get_current",
    "description": "A tool for getting current weather...",
    "parameters": "{type=object, properties={...}}"
  }
  // ... 更多工具
]
```

### 示例 2: 直接调用计算器工具

**请求**:
```bash
curl -X POST "http://localhost:8080/api/mcp/invoke?toolName=calculator_add" \
  -H "Content-Type: application/json" \
  -d '{
    "a": 10,
    "b": 20
  }'
```

**响应**:
```
30.0
```

**日志输出** (demo-core):
```
========================================
直接调用 MCP 工具
工具名称: calculator_add
参数: {a=10, b=20}
========================================
工具验证通过: calculator_add
参数 JSON: {"a":10,"b":20}
创建工具执行请求: id=xxx, name=calculator_add
正在通过 MCP 协议远程调用工具...
========================================
工具调用成功
返回结果: 30.0
========================================
```

### 示例 3: 调用天气工具

**请求**:
```bash
curl -X POST "http://localhost:8080/api/mcp/invoke?toolName=weather_get_current" \
  -H "Content-Type: application/json" \
  -d '{
    "city": "北京"
  }'
```

**响应**:
```
北京今天晴，温度15-25度，空气质量良好
```

### 示例 4: AI 聊天（自动调用工具）

**请求**:
```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "帮我计算 100 加 200 等于多少"
  }'
```

**响应**:
```json
{
  "success": true,
  "content": "100 加 200 等于 300。",
  "errorMessage": null
}
```

**工作流程**:
1. AI 收到问题："帮我计算 100 加 200"
2. AI 识别需要调用 `calculator_add` 工具
3. AI 通过 MCP 协议调用远程工具: `calculator_add(100, 200)`
4. mcp-server 返回结果: `300.0`
5. AI 生成自然语言回答: "100 加 200 等于 300。"

## 自动化测试

运行测试脚本：

```bash
./test-mcp-client.sh
```

测试内容：
1. ✅ 检查 mcp-server 是否运行
2. ✅ 检查 demo-core 是否运行
3. ✅ 获取工具列表
4. ✅ 调用计算器工具（加法）
5. ✅ 调用计算器工具（乘法）
6. ✅ 调用天气工具
7. ✅ AI 聊天（自动调用工具）

## 关键代码说明

### McpServiceImpl.invokeTool() 方法

这是实现直接远程调用 MCP 工具的核心方法。

**步骤**:

1. **验证工具存在**:
```java
List<ToolSpecification> toolSpecs = mcpClient.listTools();
ToolSpecification toolSpec = toolSpecs.stream()
        .filter(spec -> spec.name().equals(toolName))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolName));
```

2. **构建 JSON 参数**:
```java
String arguments = convertParametersToJson(parameters);
// 例如: {"a":10,"b":20}
```

3. **创建工具执行请求**:
```java
ToolExecutionRequest executionRequest = ToolExecutionRequest.builder()
        .id(UUID.randomUUID().toString())
        .name(toolName)
        .arguments(arguments)
        .build();
```

4. **通过 MCP 协议远程调用**:
```java
McpToolProvider toolProvider = McpToolProvider.builder()
        .mcpClients(mcpClient)
        .build();

String result = toolProvider.execute(executionRequest, null);
```

### MCP Client 配置

支持两种传输方式的配置：

```java
@Bean
public McpClient mcpClient() {
    McpTransport transport = createTransport();

    return new DefaultMcpClient.Builder()
            .transport(transport)
            .clientName("langchain4j-demo")
            .clientVersion("1.0.0")
            .toolExecutionTimeout(Duration.ofSeconds(30))
            .build();
}

private McpTransport createTransport() {
    if ("http".equals(transportType)) {
        // HTTP/SSE 传输
        return new HttpMcpTransport.Builder()
                .sseUrl(mcpServerUrl + "/mcp/sse")
                .timeout(Duration.ofSeconds(60))
                .build();
    } else {
        // Stdio 传输
        return new StdioMcpTransport.Builder()
                .command(Arrays.asList("java", "-jar", mcpServerJar))
                .build();
    }
}
```

## 故障排查

### 问题 1: 连接失败

**错误**: `Failed to connect to MCP Server`

**解决**:
1. 确认 mcp-server 已启动（HTTP 模式）
2. 检查端口是否正确（默认 8081）
3. 查看 demo-core 日志确认配置

### 问题 2: 工具未找到

**错误**: `Tool not found: xxx`

**解决**:
1. 调用 `/api/mcp/tools` 查看可用工具列表
2. 确认工具名称拼写正确
3. 确认 mcp-server 已正确注册工具

### 问题 3: 参数错误

**错误**: `Invalid parameters`

**解决**:
1. 检查参数类型（数字、字符串）
2. 查看工具定义的参数要求
3. 确保 JSON 格式正确

## 最佳实践

### 1. 使用 HTTP 模式部署

生产环境推荐使用 HTTP 模式：
- 两个独立服务，便于扩展
- 可以独立重启和维护
- 更好的性能和稳定性

### 2. 错误处理

```java
try {
    String result = mcpService.invokeTool(toolName, parameters);
    return ResponseEntity.ok(result);
} catch (IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(e.getMessage());
} catch (Exception e) {
    return ResponseEntity.status(500).body("Internal error: " + e.getMessage());
}
```

### 3. 日志记录

启用详细日志帮助调试：

```yaml
logging:
  level:
    org.example.langchain4jdemo: DEBUG
    dev.langchain4j: DEBUG
```

### 4. 超时配置

根据工具执行时间调整超时：

```java
.toolExecutionTimeout(Duration.ofSeconds(30))
```

## 扩展方向

### 1. 添加更多工具

在 mcp-server 中添加新工具后，demo-core 自动可用，无需修改代码。

### 2. 支持多个 MCP Server

可以配置多个 McpClient 连接不同的 MCP Server：

```java
@Bean
public McpClient calculatorServer() { ... }

@Bean
public McpClient weatherServer() { ... }
```

### 3. 工具权限控制

可以在 demo-core 层添加权限验证：

```java
if (!hasPermission(user, toolName)) {
    throw new ForbiddenException();
}
```

## 参考资源

- [LangChain4j MCP Client 文档](https://docs.langchain4j.dev/)
- [MCP 协议规范](https://spec.modelcontextprotocol.io/)
- [Spring AI MCP Server 文档](https://docs.spring.io/spring-ai/reference/)

## 总结

通过 MCP 协议，demo-core 和 mcp-server 实现了完全解耦的工具调用：

- ✅ **标准协议**: 基于 MCP 标准
- ✅ **独立服务**: 两个服务独立部署
- ✅ **远程调用**: 通过 HTTP/SSE 或 Stdio 通信
- ✅ **自动发现**: 工具列表自动获取
- ✅ **灵活扩展**: 添加工具无需修改客户端

这是一个典型的微服务架构，demo-core 作为客户端，mcp-server 作为服务端，通过标准的 MCP 协议进行通信。
