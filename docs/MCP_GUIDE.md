# MCP (Model Context Protocol) 使用指南

## 一、概述

MCP (Model Context Protocol) 是一个开放协议，用于标准化 AI 应用与外部工具/数据源之间的通信。

### 架构图 (HTTP/SSE 模式)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      主应用 (localhost:8080)                         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────────┐  │
│  │  Controller │───▶│   Service   │───▶│  ChatModel + McpClient  │  │
│  └─────────────┘    └─────────────┘    └───────────┬─────────────┘  │
└───────────────────────────────────────────────────┼─────────────────┘
                                                    │
                                          HTTP/SSE (网络通信)
                                                    │
                    ┌───────────────────────────────┴───────────────────────────┐
                    │                                                           │
                    ▼                                                           ▼
     ┌──────────────────────────────┐                        ┌──────────────────────────────┐
     │   MCP Server (localhost:8081) │                        │      第三方 MCP Server        │
     │   (独立部署的服务)              │                        │   (可部署在任意机器)          │
     │                              │                        │                              │
     │  - CalculatorTool            │                        │  - 文件读写                   │
     │  - WeatherTool               │                        │  - GitHub 操作               │
     │  - FileSystemTool            │                        │  - 数据库查询                 │
     │  - DatabaseTool              │                        │  - ...                       │
     └──────────────────────────────┘                        └──────────────────────────────┘
```

### 通信方式

| 方式 | 说明 | 适用场景 | 本项目使用 |
|------|------|----------|-----------|
| **Stdio** | 通过标准输入输出通信 | Server 作为子进程启动 | ❌ |
| **HTTP/SSE** | 通过 HTTP 协议通信 | Server 作为独立服务运行 | ✅ |

---

## 二、MCP Server 端（提供工具）

MCP Server 作为独立的 HTTP 服务运行，暴露工具供 Client 调用。

### 2.1 项目结构

```
mcp-server/
├── pom.xml
└── src/main/java/org/example/mcpserver/
    ├── McpServerApplication.java      # 启动入口
    ├── config/
    │   └── McpToolsConfig.java        # 工具注册配置
    └── tools/
        ├── CalculatorTool.java        # 计算器工具
        ├── WeatherTool.java           # 天气工具
        ├── FileSystemTool.java        # 文件系统工具
        └── DatabaseTool.java          # 数据库工具
```

### 2.2 Maven 依赖

```xml
<!-- HTTP/SSE 模式 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2.3 定义工具

使用 `@Tool` 和 `@ToolParam` 注解定义 MCP 工具：

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CalculatorTool {

    @Tool(description = "计算两个数的和")
    public int add(
        @ToolParam(description = "第一个数") int a,
        @ToolParam(description = "第二个数") int b) {
        return a + b;
    }

    @Tool(description = "计算两个数的差")
    public int subtract(
        @ToolParam(description = "被减数") int a,
        @ToolParam(description = "减数") int b) {
        return a - b;
    }

    @Tool(description = "计算两个数的积")
    public int multiply(
        @ToolParam(description = "第一个数") int a,
        @ToolParam(description = "第二个数") int b) {
        return a * b;
    }

    @Tool(description = "计算两个数的商")
    public double divide(
        @ToolParam(description = "被除数") double a,
        @ToolParam(description = "除数") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        return a / b;
    }
}
```

### 2.4 注册工具

在 `McpToolsConfig.java` 中注册工具：

```java
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider calculatorTools(CalculatorTool calculatorTool) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(calculatorTool)
            .build();
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherTool weatherTool) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherTool)
            .build();
    }
}
```

### 2.5 配置文件

`application.yml`:

```yaml
server:
  port: 8081  # MCP Server 端口

spring:
  application:
    name: mcp-server
  main:
    web-application-type: servlet  # HTTP 模式

spring.ai.mcp.server:
  name: langchain4j-demo-mcp-server
  version: 1.0.0
  sse-message-endpoint: /mcp/message  # SSE 消息端点
```

### 2.6 启动 Server

```bash
cd mcp-server

# 方式1: Maven 运行
mvn spring-boot:run

# 方式2: 打包后运行
mvn package
java -jar target/mcp-server-0.0.1-SNAPSHOT.jar
```

启动后，MCP Server 会在 `http://localhost:8081` 运行。

---

## 三、MCP Client 端（调用工具）

主应用作为 MCP Client，通过 HTTP/SSE 连接到 MCP Server。

### 3.1 添加依赖

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-mcp</artifactId>
    <version>1.0.0-beta3</version>
</dependency>
```

### 3.2 配置文件

`application.yml`:

```yaml
server:
  port: 8080

# MCP Server 配置 (HTTP/SSE 模式)
mcp:
  server:
    base-url: http://localhost:8081
```

### 3.3 配置 MCP Client

在 `McpClientConfiguration.java` 中配置：

```java
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpClientConfiguration {

    @Value("${mcp.server.base-url:http://localhost:8081}")
    private String mcpServerBaseUrl;

    /**
     * 创建 MCP Client，通过 HTTP/SSE 连接到 MCP Server
     */
    @Bean
    public McpClient mcpClient() {
        // 使用 HTTP SSE 传输
        McpTransport transport = new HttpMcpTransport.Builder()
            .baseUrl(mcpServerBaseUrl)  // MCP Server 地址
            .sseEndpoint("/sse")        // SSE 端点
            .build();

        McpClient client = new DefaultMcpClient.Builder()
            .transport(transport)
            .build();

        // 初始化连接
        client.initialize();

        return client;
    }

    /**
     * 创建 McpToolProvider，简化工具集成
     */
    @Bean
    public McpToolProvider mcpToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
            .mcpClients(List.of(mcpClient))
            .build();
    }
}
```

### 3.4 在 Service 中使用

#### 方式1: 使用 AiServices + McpToolProvider（推荐）

```java
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

@Service
public class McpServiceImpl implements McpService {

    private final Assistant assistant;

    // 定义 AI 助手接口
    interface Assistant {
        String chat(String message);
    }

    public McpServiceImpl(OpenAiChatModel chatModel, McpToolProvider mcpToolProvider) {
        // 创建带 MCP 工具的助手
        this.assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .toolProvider(mcpToolProvider)  // 自动获取 MCP Server 的所有工具
            .build();
    }

    @Override
    public McpResponse chatWithMcp(McpRequest request) {
        try {
            String response = assistant.chat(request.getMessage());
            return McpResponse.builder()
                .content(response)
                .success(true)
                .build();
        } catch (Exception e) {
            return McpResponse.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }
}
```

#### 方式2: 直接使用 McpClient

```java
import dev.langchain4j.mcp.client.McpClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

@Service
public class McpServiceImpl implements McpService {

    private final McpClient mcpClient;

    public McpServiceImpl(McpClient mcpClient) {
        this.mcpClient = mcpClient;
    }

    @Override
    public List<Map<String, Object>> listAvailableTools() {
        // 获取 MCP Server 提供的工具列表
        List<Tool> tools = mcpClient.listTools();

        return tools.stream()
            .map(tool -> Map.of(
                "name", (Object) tool.name(),
                "description", (Object) tool.description()
            ))
            .toList();
    }

    @Override
    public String invokeTool(String toolName, Map<String, Object> parameters) {
        // 直接调用 MCP 工具
        CallToolResult result = mcpClient.callTool(toolName, parameters);

        return result.content().stream()
            .filter(c -> c instanceof io.modelcontextprotocol.spec.McpSchema.TextContent)
            .map(c -> ((io.modelcontextprotocol.spec.McpSchema.TextContent) c).text())
            .collect(Collectors.joining("\n"));
    }
}
```

---

## 四、完整示例

### 4.1 启动服务

```bash
# 终端1: 启动 MCP Server (端口 8081)
cd mcp-server
mvn spring-boot:run

# 终端2: 启动主应用 (端口 8080)
cd ..
mvn spring-boot:run
```

### 4.2 用户发送请求

```bash
curl -X POST http://localhost:8080/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我计算 123 + 456 的结果"}'
```

### 4.3 处理流程

```
1. 用户请求 → 主应用 Controller (8080)
2. McpService 使用 AiServices (带 McpToolProvider)
3. ChatModel 分析用户意图，决定调用 "add" 工具
4. McpToolProvider 通过 HTTP/SSE 调用 MCP Server (8081)
5. MCP Server 执行 CalculatorTool.add(123, 456)
6. 结果通过 HTTP 返回给 ChatModel
7. ChatModel 生成最终回复
8. 返回给用户: "123 + 456 的结果是 579"
```

### 4.4 响应示例

```json
{
  "content": "123 + 456 的结果是 579",
  "success": true,
  "toolExecutions": [
    {
      "toolName": "add",
      "arguments": "{\"a\": 123, \"b\": 456}",
      "result": "579"
    }
  ]
}
```

---

## 五、部署架构

### 5.1 单机部署

```
┌─────────────────────────────────────────┐
│              同一台机器                   │
│                                         │
│  ┌─────────────────┐  ┌──────────────┐  │
│  │  主应用 :8080    │  │ MCP Server   │  │
│  │  (MCP Client)   │──│   :8081      │  │
│  └─────────────────┘  └──────────────┘  │
└─────────────────────────────────────────┘
```

### 5.2 分布式部署

```
┌─────────────────┐         ┌─────────────────┐
│   机器 A         │         │   机器 B         │
│                 │  HTTP   │                 │
│  主应用 :8080    │────────▶│  MCP Server     │
│  (MCP Client)   │         │   :8081         │
└─────────────────┘         └─────────────────┘

配置: mcp.server.base-url=http://机器B的IP:8081
```

### 5.3 多 MCP Server 部署

```
                          ┌─────────────────┐
                    ┌────▶│ MCP Server A    │
                    │     │ (Calculator)    │
┌─────────────────┐ │     └─────────────────┘
│   主应用         │─┤     ┌─────────────────┐
│  (MCP Client)   │─┼────▶│ MCP Server B    │
└─────────────────┘ │     │ (Weather)       │
                    │     └─────────────────┘
                    │     ┌─────────────────┐
                    └────▶│ MCP Server C    │
                          │ (Database)      │
                          └─────────────────┘
```

---

## 六、调试与排错

### 6.1 开启日志

主应用 `application.yml`:

```yaml
logging:
  level:
    dev.langchain4j: DEBUG
    io.modelcontextprotocol: DEBUG
```

MCP Server `application.yml`:

```yaml
logging:
  level:
    org.example.mcpserver: DEBUG
    org.springframework.ai.mcp: DEBUG
```

### 6.2 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| Connection refused | MCP Server 未启动 | 先启动 MCP Server |
| 工具未找到 | 工具未注册 | 检查 McpToolsConfig |
| 参数错误 | 参数类型不匹配 | 检查 @ToolParam 定义 |
| 连接超时 | 网络不通 | 检查防火墙和端口 |

### 6.3 测试 MCP Server 是否正常

```bash
# 检查 MCP Server 是否启动
curl http://localhost:8081/actuator/health

# 测试 SSE 端点
curl http://localhost:8081/sse
```

### 6.4 测试工具列表

```bash
# 获取可用工具列表
curl http://localhost:8080/api/mcp/tools
```

### 6.5 直接调用工具

```bash
# 直接调用 add 工具
curl -X POST "http://localhost:8080/api/mcp/invoke?toolName=add" \
  -H "Content-Type: application/json" \
  -d '{"a": 10, "b": 20}'
```

---

## 七、与 Stdio 模式对比

| 特性 | Stdio 模式 | HTTP/SSE 模式 (当前) |
|------|-----------|---------------------|
| 部署方式 | Server 作为子进程 | Server 独立部署 |
| 网络通信 | 无 (进程间通信) | 有 (HTTP) |
| 可扩展性 | 单机 | 分布式 |
| 多 Client 共享 | 不支持 | 支持 |
| 独立升级 | 不方便 | 方便 |
| 适用场景 | 本地开发/测试 | 生产环境 |

---

## 八、参考链接

- [MCP 官方文档](https://modelcontextprotocol.io/)
- [Spring AI MCP 文档](https://docs.spring.io/spring-ai/reference/api/mcp.html)
- [LangChain4j MCP 文档](https://docs.langchain4j.dev/integrations/mcp)
- [MCP Servers 列表](https://github.com/modelcontextprotocol/servers)
