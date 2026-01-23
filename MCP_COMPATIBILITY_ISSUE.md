# MCP 兼容性问题说明

## 问题描述

前端查看 MCP tools 时报 500 错误。

**根本原因**: LangChain4j MCP Client 和 Spring AI MCP Server 使用不同的 MCP 协议实现，导致不兼容。

## 技术细节

### 1. 两种不同的 MCP 实现

#### LangChain4j MCP (langchain4j-demo-core)
- 依赖: `dev.langchain4j:langchain4j-mcp:1.0.0-beta3`
- Client 实现: `DefaultMcpClient`
- 协议: LangChain4j 自己的 MCP 实现
- SSE 端点期望: `/sse`

#### Spring AI MCP (mcp-server)
- 依赖: `org.springframework.ai:spring-ai-starter-mcp-server-webmvc:1.0.0`
- Server 实现: Spring AI MCP Server
- 协议: Spring AI 的 MCP 实现
- SSE 端点: `/mcp/message`

### 2. 协议不兼容

这两个实现虽然都声称是 MCP (Model Context Protocol)，但实际上:
- 使用不同的消息格式
- 使用不同的端点路径
- 使用不同的序列化方式
- 底层实现完全不同

类似于同样是 HTTP，但 REST API 和 GraphQL 无法直接互操作。

## 解决方案

有三种方案可选:

### 方案 1: 在 langchain4j-demo-core 中本地实现工具 (推荐)

不使用 MCP 远程调用，直接在 langchain4j-demo-core 中实现工具。

**优点**:
- ✅ 简单直接
- ✅ 性能更好（无网络开销）
- ✅ 不需要额外的 MCP Server
- ✅ LangChain4j 原生支持

**实现步骤**:
1. 在 langchain4j-demo-core 中创建工具类
2. 使用 LangChain4j 的 `@Tool` 注解
3. 通过 `AiServices` 自动集成

**示例**:
```java
// WeatherTool.java
@Component
public class WeatherTool {

    @Tool("获取指定城市的天气信息")
    public String getWeather(@P("城市名称") String city) {
        // 实现天气查询逻辑
        return "北京今天晴，温度15-25度";
    }
}

// ChatService.java
interface AssistantService {
    String chat(String message);
}

AssistantService assistant = AiServices.builder(AssistantService.class)
    .chatLanguageModel(chatModel)
    .tools(weatherTool)  // 直接注入工具
    .build();
```

### 方案 2: 使用 LangChain4j MCP Server

创建一个基于 LangChain4j 的 MCP Server（而不是 Spring AI 的）。

**注意**: LangChain4j 1.0.0-beta3 主要提供 MCP Client，Server 功能可能不完整。

### 方案 3: 创建协议适配层

在两者之间创建一个适配层，转换协议格式。

**缺点**:
- ❌ 复杂度高
- ❌ 维护成本大
- ❌ 性能开销

## 建议实现方案

**推荐使用方案 1：本地工具实现**

理由:
1. LangChain4j 对本地工具支持非常好
2. 性能更优（无网络调用）
3. 代码更简单易维护
4. 不需要额外的 Server

### 实施计划

#### 步骤 1: 移除 MCP 远程调用相关代码

1. 保留 `McpService` 接口和 `McpServiceImpl`（以免影响前端API）
2. 修改实现，不再使用 `McpClient`，而是直接调用本地工具

#### 步骤 2: 实现本地工具

在 `langchain4j-demo-core` 中创建:
```
src/main/java/org/example/langchain4jdemo/
├── tools/
│   ├── WeatherTool.java
│   ├── CalculatorTool.java
│   └── ... (其他工具)
```

#### 步骤 3: 集成工具到服务

修改 `McpServiceImpl`:
```java
@Service
@RequiredArgsConstructor
public class McpServiceImpl implements McpService {

    private final ChatLanguageModel chatModel;
    private final List<Object> tools;  // 注入所有工具

    @Override
    public McpResponse chatWithMcp(McpRequest request) {
        // 使用 AiServices 构建助手
        AssistantService assistant = AiServices.builder(AssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(tools)
            .build();

        String response = assistant.chat(request.getMessage());
        return McpResponse.builder()
                .success(true)
                .content(response)
                .build();
    }
}
```

#### 步骤 4: 工具列表API

修改 `listAvailableTools()` 使用反射获取 `@Tool` 注解的方法。

## MCP Server 模块的未来

虽然当前 `mcp-server` 模块无法与 `langchain4j-demo-core` 互操作，但它仍然有价值:

1. **独立的 MCP Server**: 可以被其他支持 Spring AI MCP 的客户端使用
2. **学习参考**: 展示了如何使用 Spring AI 创建 MCP Server
3. **未来兼容**: 如果 LangChain4j 和 Spring AI 未来统一 MCP 协议，可以重新启用

## 总结

**问题根源**: 协议不兼容

**解决方案**: 使用本地工具实现，不依赖 MCP 远程调用

**下一步**:
1. 在 langchain4j-demo-core 中实现本地工具
2. 修改 McpServiceImpl 使用本地工具
3. 更新文档说明架构变更

---

**更新时间**: 2026-01-21
**问题状态**: 已识别，待实施解决方案
