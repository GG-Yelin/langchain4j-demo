# 使用官方 MCP SDK 的简单实现方案

## 你说得对！

我之前的实现太复杂了。如果使用官方的 MCP SDK，应该可以像使用 `@Tool` 一样简单！

## 官方 MCP SDK

Maven Central 上有官方的 MCP SDK：

```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webmvc</artifactId>
    <version>0.16.0</version>
</dependency>
```

这个 SDK 提供：
- `McpServerSession` - MCP Server 会话
- `McpServerTransport` - 传输层
- `WebMvcSseServerTransportProvider` - Spring WebMVC 的 SSE 支持

## 理想的简单实现

**应该**像这样简单：

### 1. 定义工具（使用 @Tool）

```java
@Component
public class WeatherTool {

    @Tool("Get weather for a city")
    public String getWeather(String city) {
        return "北京今天晴，温度15-25度";
    }
}
```

### 2. 配置 MCP Server

```java
@Configuration
public class McpServerConfig {

    @Bean
    public McpServer mcpServer(List<Object> tools) {
        return McpServer.builder()
                .transport(new WebMvcSseServerTransport())
                .tools(tools)  // 自动扫描 @Tool 注解
                .build();
    }
}
```

### 3. 完成！

MCP Server 自动：
- 扫描 @Tool 注解的方法
- 生成工具的 JSON Schema
- 处理 JSON-RPC 2.0 消息
- 通过 SSE 传输响应

## 为什么我之前实现那么复杂？

因为我**手动实现**了所有 MCP 协议的细节：
- ❌ 手动解析 JSON-RPC 2.0
- ❌ 手动管理 SSE 连接
- ❌ 手动路由 MCP 方法
- ❌ 手动序列化/反序列化

**应该直接使用官方 SDK！**

## 建议

我们有两个选择：

### 选项 A：使用官方 MCP SDK（推荐）✅

**优点：**
- ✅ 简单、优雅
- ✅ 官方维护
- ✅ 符合标准
- ✅ 像使用 `@Tool` 一样简单

**工作量：**
- 添加依赖
- 配置 MCP Server
- 定义工具（已有）
- 约 50-100 行代码

### 选项 B：保持当前手动实现

**优点：**
- ✅ 完全控制
- ✅ 深入理解 MCP 协议
- ✅ 已经实现完成

**缺点：**
- ❌ 代码复杂（约 500 行）
- ❌ 需要自己维护

## 你希望怎么做？

1. **使用官方 SDK 重新实现**（更简单）
   - 我会删除手动实现的代码
   - 使用官方 MCP SDK
   - 像 `@Tool` 一样简单

2. **保持当前实现**（已完成）
   - 虽然复杂，但功能完整
   - 可以直接使用

请告诉我你的选择！如果选择 1，我会用官方 SDK 重新实现一个简洁的版本。
