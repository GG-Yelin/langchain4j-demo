# 远程 MCP 解决方案

## 问题分析

### 协议不兼容的根本原因

1. **LangChain4j MCP Client (1.0.0-beta3)**
   - 主要提供 Client 端实现
   - Server 端支持不完整
   - 需要特定的 SSE 消息格式

2. **Spring AI MCP Server (1.0.0)**
   - 提供 Server 端实现
   - 使用 Spring AI 的协议格式
   - 与 LangChain4j Client 不兼容

3. **MCP 协议现状**
   - 标准还在演进中
   - 不同框架实现不一致
   - 互操作性有限

## 推荐方案

由于直接的 MCP 协议兼容性问题，我建议采用 **HTTP REST API** 方案实现远程工具调用：

### 方案 A: REST API 远程工具服务（推荐）

#### 架构
```
langchain4j-demo-core          mcp-server (REST API)
┌─────────────────┐           ┌──────────────────┐
│  McpClient      │  HTTP     │  /api/tools      │
│  (Wrapper)      │──────────>│  /api/execute    │
│                 │           │                  │
│  LLM + Tools    │           │  Tools:          │
│                 │           │  - WeatherTool   │
│                 │           │  - CalculatorTool│
└─────────────────┘           └──────────────────┘
```

#### 优点
✅ 简单可靠，HTTP REST API 成熟稳定
✅ 易于调试和监控
✅ 不依赖复杂的 MCP 协议
✅ 可以自定义错误处理
✅ 支持任何 HTTP 客户端

#### 实现步骤
1. **mcp-server**: 提供 REST API
   - `GET /api/tools` - 列出所有工具
   - `POST /api/execute` - 执行工具

2. **langchain4j-demo-core**: 创建 REST Client
   - 包装 HTTP 调用
   - 实现 McpClient 接口（或类似接口）
   - 与 LLM 集成

### 方案 B: 实现标准 MCP 协议（复杂）

尝试手动实现符合 LangChain4j 期望的 MCP Server：

#### 挑战
- ❌ LangChain4j 1.0.0-beta3 MCP Server 支持不完整
- ❌ 需要深入了解 SSE 消息格式
- ❌ 协议细节文档不全
- ❌ 调试困难
- ❌ 维护成本高

#### 可能性
- 研究 LangChain4j MCP Client 源码
- 实现符合其期望的 SSE 端点
- 手动构造 MCP 协议消息

### 方案 C: 使用同一框架（最简单）

将整个项目统一为 Spring AI 或全部使用 LangChain4j：

#### 选项 1: 全部 Spring AI
- mcp-server: Spring AI MCP Server
- langchain4j-demo-core: 改用 Spring AI MCP Client

#### 选项 2: 全部 LangChain4j
- mcp-server: 实现 LangChain4j 兼容的 Server
- langchain4j-demo-core: 保持 LangChain4j MCP Client

## 我的建议

### 最佳选择：方案 A (REST API)

原因：
1. **实用性**: HTTP REST API 是成熟可靠的远程调用方式
2. **兼容性**: 不依赖特定框架的 MCP 实现
3. **可维护性**: 代码简单，易于理解和维护
4. **扩展性**: 未来可以轻松添加更多工具
5. **调试性**: HTTP 请求易于测试和监控

### 实现细节

#### 1. MCP Server (REST API)

**API 设计**:
```
GET /api/tools
Response: [
  {
    "name": "getWeather",
    "description": "获取城市天气",
    "parameters": {
      "city": {"type": "string", "description": "城市名称"}
    }
  },
  ...
]

POST /api/execute
Request: {
  "toolName": "getWeather",
  "arguments": {"city": "北京"}
}
Response: {
  "success": true,
  "result": "北京今天晴..."
}
```

#### 2. LangChain4j-demo-core

创建 `RemoteToolService`:
```java
@Service
public class RemoteToolService {

    private final RestTemplate restTemplate;
    private final String mcpServerUrl = "http://localhost:8081";

    public List<ToolSpecification> listTools() {
        // HTTP GET to /api/tools
        // 转换为 ToolSpecification
    }

    public String executeTool(String name, String arguments) {
        // HTTP POST to /api/execute
        // 返回结果
    }
}
```

与 LLM 集成：
```java
// 获取远程工具列表
List<ToolSpecification> remoteTools = remoteToolService.listTools();

// 创建工具执行器
ToolExecutor toolExecutor = (request) -> {
    return remoteToolService.executeTool(
        request.name(),
        request.arguments()
    );
};

// 使用 AiServices 或手动处理工具调用
```

## 下一步

请选择方案：

**A. REST API 方案** (推荐)
- 我会实现 mcp-server 的 REST API
- 实现 langchain4j-demo-core 的 HTTP Client
- 实现工具列表和执行逻辑

**B. 标准 MCP 协议** (复杂)
- 深入研究 LangChain4j MCP Client 源码
- 实现兼容的 SSE Server
- 风险较高，可能仍然不兼容

**C. 框架统一** (需重构)
- 选择一个框架（Spring AI 或 LangChain4j）
- 大规模重构代码
- 工作量较大

---

**我的推荐**: 选择方案 A，实现简单可靠的 REST API 远程工具调用。

您想采用哪个方案？
