# 真正的 MCP 协议实现方案

## 问题说明

当前实现使用的是简单的 HTTP REST API，而不是标准的 **MCP (Model Context Protocol)** 协议。

MCP 协议的特点：
- 基于 **JSON-RPC 2.0**
- 使用 **SSE (Server-Sent Events)** 或 **stdio** 传输
- 有标准的消息格式和方法

## MCP 协议示例

### 1. JSON-RPC 2.0 格式

**请求:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}
```

**响应:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "get_weather",
        "description": "Get weather information",
        "inputSchema": {
          "type": "object",
          "properties": {
            "city": {"type": "string"}
          },
          "required": ["city"]
        }
      }
    ]
  }
}
```

### 2. MCP 标准方法

- `initialize` - 初始化连接
- `tools/list` - 列出可用工具
- `tools/call` - 调用工具
- `resources/list` - 列出资源
- `prompts/list` - 列出提示词

## 实现方案

### 方案 A: 使用 LangChain4j 内置的 MCP Server（推荐）

LangChain4j 1.0.0-beta3 可能已经提供了 MCP Server 的实现。

**步骤：**
1. 查看 `langchain4j-mcp` 包中是否有 Server 实现
2. 如果有，使用它创建 MCP Server
3. 注册工具并启动服务

**优点:**
- ✅ 与 LangChain4j Client 完美兼容
- ✅ 无需学习新 SDK
- ✅ 版本匹配

### 方案 B: 使用官方 MCP SDK

使用 `io.modelcontextprotocol.sdk:mcp` 创建标准 MCP Server。

**依赖:**
```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
    <version>0.16.0</version>
</dependency>
```

**优点:**
- ✅ 标准 MCP 实现
- ✅ 遵循 MCP 规范

**挑战:**
- ❓ 需要确认与 LangChain4j 的兼容性
- ❓ 可能需要适配

### 方案 C: 手动实现 JSON-RPC 2.0 over SSE

完全手动实现 MCP 协议。

**需要实现:**
1. SSE 端点
2. JSON-RPC 2.0 消息处理
3. MCP 方法路由
4. 工具注册和调用

**优点:**
- ✅ 完全控制
- ✅ 可精确匹配 LangChain4j

**缺点:**
- ❌ 工作量大
- ❌ 容易出错

## 推荐的实施步骤

### 第一步：探索 langchain4j-mcp

```bash
# 查看 langchain4j-mcp 包内容
mvn dependency:unpack -Dartifact=dev.langchain4j:langchain4j-mcp:1.0.0-beta3

# 或者查看本地 Maven 仓库
ls ~/.m2/repository/dev/langchain4j/langchain4j-mcp/1.0.0-beta3/
```

### 第二步：查找 Server 类

查找是否有以下类：
- `McpServer`
- `DefaultMcpServer`
- `SseMcpServer`
- `McpToolRegistry`

### 第三步：实现 MCP Server

根据找到的类实现 MCP Server：

```java
// 伪代码示例
McpServer server = new DefaultMcpServer.Builder()
    .toolRegistry(toolRegistry)
    .port(8081)
    .sseEndpoint("/mcp/sse")
    .build();

server.registerTool(weatherTool);
server.registerTool(calculatorTool);
server.start();
```

### 第四步：客户端连接

```java
// langchain4j-demo-core
HttpMcpTransport transport = new HttpMcpTransport.Builder()
    .sseUrl("http://localhost:8081/mcp/sse")
    .build();

McpClient client = new DefaultMcpClient.Builder()
    .transport(transport)
    .build();
```

## 当前状态

目前我实现的是简化版的 HTTP REST API，不是真正的 MCP 协议。

**现有实现:**
- ✅ HTTP REST API
- ✅ 简单的 JSON 请求/响应
- ❌ 不是 JSON-RPC 2.0
- ❌ 不是 SSE 传输
- ❌ 不符合 MCP 规范

## 下一步行动

**选项 1: 探索 LangChain4j MCP Server**
我可以深入研究 `langchain4j-mcp` 包，查看是否有内置的 Server 实现。

**选项 2: 使用官方 MCP SDK**
使用 `io.modelcontextprotocol.sdk:mcp` 实现标准 MCP Server。

**选项 3: 手动实现 MCP 协议**
完全手动实现 JSON-RPC 2.0 over SSE。

**选项 4: 保持当前实现**
如果你只需要远程工具调用功能，当前的 HTTP REST API 实现也可以工作，只是不符合 MCP 标准协议。

## 你的选择？

请告诉我你希望：

**A. 实现真正的 MCP 协议** (使用 JSON-RPC 2.0 + SSE)
- 我会探索 LangChain4j 的 MCP Server 实现
- 或使用官方 MCP SDK

**B. 保持当前的 HTTP REST API**
- 虽然不是标准 MCP，但功能完整
- 更简单易懂

**C. 先让我探索 langchain4j-mcp 包的内容**
- 查看是否有现成的 Server 实现
- 然后决定最佳方案

请告诉我你的选择！
