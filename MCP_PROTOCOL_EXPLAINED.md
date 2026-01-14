# MCP (Model Context Protocol) 详解

## 核心理解

**MCP 就是一个远程工具调用的标准化协议。**

可以类比为：
- **HTTP** 是 Web 服务的标准协议
- **gRPC** 是微服务之间的 RPC 协议
- **MCP** 是 AI 与工具服务器之间的标准协议

## MCP 的本质

### 1. 协议定义

MCP 定义了一套标准化的通信规范，用于：
- AI 应用发现可用的工具
- 调用远程工具
- 获取工具执行结果

### 2. 架构模式

```
┌─────────────┐         MCP Protocol        ┌──────────────────┐
│             │◄──────────────────────────►│                  │
│  AI 应用     │                             │  MCP Tool Server │
│ (LangChain) │  - 工具发现 (listTools)     │   (独立服务)      │
│             │  - 工具调用 (callTool)      │                  │
│             │  - 获取结果                  │                  │
└─────────────┘                             └──────────────────┘
```

### 3. 与其他方式的对比

| 特性 | @Tool 注解 | MCP 协议 |
|------|-----------|---------|
| **位置** | 本地/进程内 | 远程/独立服务 |
| **语言** | 必须 Java | 任意语言 |
| **部署** | 与应用一起 | 独立部署 |
| **通信** | 方法调用 | 网络协议 |
| **类比** | 直接函数调用 | HTTP API 调用 |

## MCP 的工作流程

### 流程图

```
┌─────────┐
│ 用户询问 │
│"北京天气"│
└────┬────┘
     │
     ▼
┌─────────────────┐
│  AI 分析需求     │ → 需要调用天气工具
└────┬────────────┘
     │
     ▼
┌─────────────────────┐
│ 1. 发送 listTools()  │ ──MCP──► ┌──────────────┐
│    请求到 MCP 服务器  │          │ MCP 工具服务器 │
└─────────────────────┘          │ (Python/JS)   │
                                │               │
┌─────────────────────┐          │ 工具列表:     │
│ 2. 收到工具列表      │ ◄──MCP── │ - get_weather│
│    [{name: "get_    │          │ - get_news   │
│      weather",...}] │          │ - ...        │
└────┬────────────────┘          └──────────────┘
     │
     ▼
┌─────────────────────┐
│ 3. AI 决定调用       │
│    get_weather       │
│    参数: {city:"北京"}│
└────┬────────────────┘
     │
     ▼
┌─────────────────────┐
│ 4. callTool()       │ ──MCP──► ┌──────────────┐
│    name: "get_      │          │ 执行 Python   │
│    weather"         │          │ get_weather() │
│    args: {...}      │          │ 函数          │
└─────────────────────┘          └──────────────┘
                                      │
┌─────────────────────┐               │
│ 5. 收到执行结果      │ ◄──MCP────────┘
│    "北京: 晴, 25°C"  │
└────┬────────────────┘
     │
     ▼
┌─────────────────────┐
│ 6. AI 综合结果      │
│    生成友好回复      │
└────┬────────────────┘
     │
     ▼
┌─────────────────────┐
│ "北京今天天气很好，  │
│  晴天，温度25度"     │
└─────────────────────┘
```

## MCP 协议的核心消息

### 1. 工具发现 (listTools)

**请求**：
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

**响应**：
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "get_weather",
        "description": "获取指定城市的天气信息",
        "inputSchema": {
          "type": "object",
          "properties": {
            "city": {
              "type": "string",
              "description": "城市名称"
            },
            "unit": {
              "type": "string",
              "enum": ["celsius", "fahrenheit"],
              "description": "温度单位"
            }
          },
          "required": ["city"]
        }
      },
      {
        "name": "search_news",
        "description": "搜索新闻",
        "inputSchema": {
          "type": "object",
          "properties": {
            "keyword": {
              "type": "string",
              "description": "搜索关键词"
            }
          },
          "required": ["keyword"]
        }
      }
    ]
  }
}
```

### 2. 工具调用 (callTool)

**请求**：
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "get_weather",
    "arguments": {
      "city": "北京",
      "unit": "celsius"
    }
  }
}
```

**响应**：
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "北京天气：晴，温度 25°C，湿度 60%，风速 3m/s"
      }
    ],
    "isError": false
  }
}
```

## MCP 的优势

### 1. 语言无关

MCP 服务器可以用任何语言实现：

```
┌──────────────┐
│  Java 应用    │
│ (LangChain4j) │
└──────┬───────┘
       │ MCP
       ├─────────► Python MCP Server (数据分析工具)
       ├─────────► Node.js MCP Server (网页抓取工具)
       ├─────────► Go MCP Server (系统监控工具)
       └─────────► Rust MCP Server (高性能计算工具)
```

### 2. 独立部署

```
应用层：    ┌────────┐  ┌────────┐  ┌────────┐
           │App 实例1│  │App 实例2│  │App 实例3│
           └───┬────┘  └───┬────┘  └───┬────┘
               │           │           │
               └───────────┼───────────┘
                           │ MCP
工具层：    ┌──────────────┴──────────────┐
           │                              │
       ┌───▼───┐  ┌────────┐  ┌────────┐
       │Weather│  │Database│  │Payment │
       │ Tools │  │ Tools  │  │ Tools  │
       └───────┘  └────────┘  └────────┘
```

**好处**：
- 工具服务独立扩展
- 应用无需重启就能添加新工具
- 不同应用共享同一套工具

### 3. 标准化

遵循统一的协议规范：
- **发现机制**：统一的工具列表格式
- **调用方式**：标准的 JSON-RPC 2.0
- **错误处理**：统一的错误码和消息
- **数据格式**：JSON Schema 定义参数

### 4. 安全隔离

```
┌─────────────┐        网络边界         ┌──────────────┐
│             │                        │  危险操作:    │
│  AI 应用     │◄──────MCP────────────►│  - 文件系统   │
│  (沙箱)      │      (可控权限)        │  - 数据库     │
│             │                        │  - 外部API    │
└─────────────┘                        └──────────────┘
```

工具在独立进程/服务中运行，不会影响主应用。

## MCP vs 其他方式

### 场景对比

#### 场景1：简单计算

**@Tool 注解**（推荐）：
```java
public class Calculator {
    @Tool("计算加法")
    public double add(double a, double b) {
        return a + b;
    }
}
```

**MCP**（过度设计）：
- 需要启动 MCP 服务器
- 网络通信开销
- 额外的运维成本

**结论**：简单工具用 @Tool，不需要 MCP。

---

#### 场景2：Python 数据分析库

**@Tool 注解**（不适用）：
- 无法调用 Python 库（pandas, numpy）
- 需要用 JPython 或 GraalVM（复杂）

**MCP**（完美适配）：
```python
# mcp_server.py
import pandas as pd
import numpy as np

@mcp_tool
def analyze_data(csv_data):
    df = pd.read_csv(csv_data)
    return df.describe().to_json()
```

**结论**：跨语言工具必须用 MCP。

---

#### 场景3：企业内部微服务

**@Tool 注解**（不适用）：
- 服务在不同服务器
- 需要服务发现、负载均衡

**MCP**（适配）：
```
AI App → MCP → API Gateway → 订单服务
                           → 库存服务
                           → 支付服务
```

**结论**：微服务架构用 MCP 统一封装。

---

#### 场景4：动态工具插件

**@Tool 注解**（不适用）：
- 需要重新编译、部署
- 无法动态加载

**MCP**（完美）：
- 启动新的 MCP 服务器即可
- 应用自动发现新工具
- 零停机添加功能

**结论**：插件系统用 MCP。

## MCP 的实现方式

### 1. 传输层

MCP 支持多种传输方式：

#### stdio（标准输入输出）
```bash
# 启动 MCP 服务器作为子进程
java -jar mcp-server.jar
# 通过 stdin/stdout 通信
```

**特点**：
- 简单、轻量
- 进程级隔离
- 适合本地工具

#### HTTP/SSE
```
POST http://localhost:3000/mcp/tools/list
POST http://localhost:3000/mcp/tools/call
```

**特点**：
- 网络通信
- 跨机器部署
- 支持负载均衡

#### WebSocket
```
ws://localhost:3000/mcp
```

**特点**：
- 双向通信
- 实时推送
- 适合流式响应

### 2. 你的项目实现

查看 `McpServiceImpl.java`：

```java
@Service
public class McpServiceImpl implements McpService {

    private final McpClient mcpClient;  // MCP 客户端

    public McpResponse chat(String userMessage) {
        // 1. 获取工具列表
        ListToolsResult tools = mcpClient.listTools();

        // 2. 转换为 LangChain4j 格式
        List<ToolSpecification> toolSpecs = convertTools(tools);

        // 3. AI 决定调用哪个工具
        ChatResponse response = chatModel.chat(request);

        // 4. 通过 MCP 调用工具
        if (response.aiMessage().hasToolExecutionRequests()) {
            for (ToolExecutionRequest req : requests) {
                CallToolResult result = mcpClient.callTool(
                    req.name(),
                    parseArguments(req.arguments())
                );
                // ... 处理结果
            }
        }

        return mcpResponse;
    }
}
```

这就是标准的 MCP 客户端实现。

## MCP 生态系统

### 官方 MCP 服务器

Anthropic（Claude 的公司）提供了很多官方 MCP 服务器：

```bash
# 文件系统工具
npx @modelcontextprotocol/server-filesystem

# Git 操作工具
npx @modelcontextprotocol/server-git

# 数据库工具
npx @modelcontextprotocol/server-postgres

# Brave 搜索
npx @modelcontextprotocol/server-brave-search

# Google Maps
npx @modelcontextprotocol/server-google-maps
```

### 社区 MCP 服务器

```
github.com/awesome-mcp-servers

- Slack MCP Server
- Notion MCP Server
- Google Drive MCP Server
- Docker MCP Server
- Kubernetes MCP Server
- ...
```

### 自定义 MCP 服务器

**Python 示例**：
```python
from mcp.server import MCPServer
from mcp.types import Tool

app = MCPServer(__name__)

@app.tool()
async def get_weather(city: str) -> str:
    """获取天气信息"""
    # 调用天气 API
    return f"{city}的天气是晴天"

@app.tool()
async def translate(text: str, target_lang: str) -> str:
    """翻译文本"""
    # 调用翻译 API
    return translated_text

if __name__ == "__main__":
    app.run()
```

**Node.js 示例**：
```javascript
import { MCPServer } from '@modelcontextprotocol/sdk';

const server = new MCPServer({
  name: 'my-tools',
  version: '1.0.0'
});

server.tool({
  name: 'scrape_website',
  description: '抓取网页内容',
  inputSchema: {
    type: 'object',
    properties: {
      url: { type: 'string' }
    }
  },
  handler: async ({ url }) => {
    // 抓取网页
    return { content: '...' };
  }
});

server.listen(3000);
```

## 总结

### MCP 的定位

```
┌────────────────────────────────────────┐
│            AI 应用层                    │
│  (LangChain4j, LangGraph, etc.)        │
└──────────────┬─────────────────────────┘
               │
        ┌──────┴──────┐
        │             │
    本地工具      远程工具
    (@Tool)       (MCP)
        │             │
        ├─────────┐   ├──────────┐
        │ Java类  │   │跨语言服务 │
        └─────────┘   │独立部署   │
                     │标准协议   │
                     └──────────┘
```

### 何时使用 MCP

✅ **应该用 MCP**：
- 跨语言工具（Python、Node.js、Go 等）
- 独立部署的服务
- 需要动态加载的插件
- 微服务架构
- 安全隔离的危险操作
- 社区共享的工具

❌ **不需要 MCP**：
- 简单的 Java 工具
- 进程内计算
- 无状态的纯函数
- 快速开发原型

### 类比理解

- **@Tool** = 直接调用库函数（import）
- **MCP** = 调用 REST API（HTTP）

就像你不会为简单的字符串处理去写个 HTTP API，但对于复杂的外部服务，你会用 HTTP 协议一样。

**MCP 就是 AI 工具调用领域的 HTTP 协议！**
