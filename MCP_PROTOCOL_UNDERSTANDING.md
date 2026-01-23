# MCP 协议正确理解

## 我之前的错误理解

❌ **错误**: 建议用 HTTP REST API 实现远程调用
✅ **正确**: MCP 是基于 JSON-RPC 2.0 的专门协议，通过 SSE 或 stdio 传输

## MCP 协议本质

### 1. 协议基础

**MCP = JSON-RPC 2.0 over SSE/stdio**

```json
// 请求示例
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list",
  "params": {}
}

// 响应示例
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "get_weather",
        "description": "Get weather for a city",
        "inputSchema": {
          "type": "object",
          "properties": {
            "city": {"type": "string"}
          }
        }
      }
    ]
  }
}
```

### 2. 传输方式

#### SSE (Server-Sent Events)
- 用于远程服务器
- 客户端连接到 Server 的 SSE 端点
- Server 通过 SSE 推送消息
- 双向通信（SSE + POST）

#### stdio
- 用于本地进程
- 通过标准输入/输出通信
- 适合子进程调用

### 3. 核心方法

MCP 定义的标准方法：
- `initialize` - 初始化连接
- `tools/list` - 列出工具
- `tools/call` - 调用工具
- `resources/list` - 列出资源
- `prompts/list` - 列出提示词

## 当前问题分析

### LangChain4j MCP Client (1.0.0-beta3)

**期望**:
- MCP 协议的特定版本
- 特定的 JSON-RPC 消息格式
- 特定的 SSE 端点行为

**实现**:
```java
HttpMcpTransport transport = new HttpMcpTransport.Builder()
    .sseUrl("http://localhost:8081/sse")
    .build();

McpClient client = new DefaultMcpClient.Builder()
    .transport(transport)
    .build();
```

### Spring AI MCP Server (基于 MCP SDK 0.10.0)

**提供**:
- MCP SDK 0.10.0 的实现
- Spring AI 特定的配置
- 可能与 LangChain4j 期望的版本不匹配

### 版本不兼容

```
LangChain4j MCP Client       Spring AI MCP Server
  (期望的 MCP 版本?)    !=    (MCP SDK 0.10.0)
```

## 解决方案

### 方案 1: 使用官方 MCP SDK 创建Server（推荐）

使用 `io.modelcontextprotocol.sdk:mcp` 创建符合标准的 MCP Server：

```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
    <version>0.16.0</version> <!-- 或者尝试与 LangChain4j 匹配的版本 -->
</dependency>
```

**优点**:
- ✅ 标准的 MCP 协议实现
- ✅ 可能与 LangChain4j 兼容
- ✅ 符合 MCP 规范

**挑战**:
- 需要学习官方 SDK 的使用
- 需要确保版本匹配

### 方案 2: 手动实现 MCP Server

自己实现符合 MCP 协议的 Server：

**需要实现**:
1. SSE 端点处理
2. JSON-RPC 2.0 消息解析
3. MCP 方法处理 (`tools/list`, `tools/call`)
4. 工具注册和执行

**优点**:
- ✅ 完全控制实现
- ✅ 可以精确匹配 LangChain4j 期望

**缺点**:
- ❌ 工作量大
- ❌ 容易出错
- ❌ 需要深入理解 MCP 协议

### 方案 3: 修改 Spring AI MCP Server（不推荐）

尝试修改现有的 Spring AI MCP Server 来匹配 LangChain4j：

**问题**:
- ❌ Spring AI 和 LangChain4j 是不同的框架
- ❌ 可能需要大量修改
- ❌ 维护困难

### 方案 4: Stdio 模式（临时方案）

使用 stdio 传输而不是 SSE：

```java
// LangChain4j Client
StdioMcpTransport transport = new StdioMcpTransport.Builder()
    .command("java", "-jar", "mcp-server.jar")
    .build();
```

**优点**:
- ✅ 避开 SSE 的复杂性
- ✅ 可能更容易调试

**缺点**:
- ❌ 不是真正的远程服务
- ❌ 每次调用都启动新进程

## 我的推荐

### 立即可行的方案：使用官方 MCP SDK

1. **修改 mcp-server 的 pom.xml**
   ```xml
   <dependency>
       <groupId>io.modelcontextprotocol.sdk</groupId>
       <artifactId>mcp</artifactId>
       <version>0.16.0</version>
   </dependency>
   ```

2. **实现 MCP Server**
   - 使用官方 SDK 的 Server 类
   - 注册工具
   - 启动 SSE 端点

3. **测试兼容性**
   - 启动 mcp-server
   - langchain4j-demo-core 的 McpClient 连接
   - 验证是否能正常通信

### 如果官方 SDK 不兼容

如果官方 SDK 仍然不匹配，我会：
1. 分析 LangChain4j MCP Client 的源码
2. 确定它期望的确切消息格式
3. 手动实现一个简单的 MCP Server

## 下一步

您希望我：

**A. 使用官方 MCP SDK 实现 Server** (推荐)
- 基于标准的 MCP SDK
- 最符合 MCP 规范
- 成功概率较高

**B. 手动实现 MCP Server**
- 完全自定义
- 精确匹配 LangChain4j
- 工作量较大

**C. 先测试 Stdio 模式**
- 快速验证可行性
- 避开 SSE 复杂性
- 后续再改为 SSE

请告诉我您的选择，或者我直接尝试方案 A？
