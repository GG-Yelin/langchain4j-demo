# MCP Server - Spring AI 官方实现

基于 **Spring AI MCP Server WebMVC Starter** 的标准 MCP Server 实现。

## 特性

- ✅ 使用 Spring AI 官方 MCP Server Starter
- ✅ 基于 `@Tool` 注解的工具定义
- ✅ 自动工具注册和发现
- ✅ 支持 HTTP WebMVC 传输
- ✅ STATELESS 协议模式
- ✅ 完全符合 MCP 2024-11-05 协议标准

## 技术栈

- **Spring Boot**: 3.4.1
- **Spring AI**: 1.1.0
- **Spring AI MCP Server WebMVC Starter**: 1.1.0
- **Java**: 17

## 快速开始

### 1. 构建项目

```bash
cd mcp-server
mvn clean package
```

### 2. 启动服务

```bash
java -jar target/mcp-server-0.0.1-SNAPSHOT.jar
```

或使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8081`

### 3. 测试服务

服务启动后，Spring AI MCP Server 会自动暴露标准的 MCP 端点。

## 架构设计

```
mcp-server/
├── src/main/java/org/example/mcpserver/
│   ├── McpServerApplication.java    # 主应用类，注册工具
│   └── tools/                       # 工具实现
│       ├── CalculatorTool.java      # 计算器工具
│       └── WeatherTool.java         # 天气工具
├── src/main/resources/
│   └── application.yml              # 配置文件
└── pom.xml
```

## 可用工具

### 计算器工具 (CalculatorTool)

1. **calculator_add** - 两数相加
   - 参数: `a` (double), `b` (double)

2. **calculator_subtract** - 两数相减
   - 参数: `a` (double), `b` (double)

3. **calculator_multiply** - 两数相乘
   - 参数: `a` (double), `b` (double)

4. **calculator_divide** - 两数相除
   - 参数: `a` (double), `b` (double)

5. **calculator_power** - 计算幂
   - 参数: `base` (double), `exponent` (double)

6. **calculator_sqrt** - 计算平方根
   - 参数: `number` (double)

### 天气工具 (WeatherTool)

1. **weather_get_current** - 查询当前天气
   - 参数: `city` (string)

2. **weather_get_forecast** - 查询天气预报
   - 参数: `city` (string), `days` (int)

## 如何添加新工具

### 1. 创建工具类

使用 `@Service` 注解标记类，使用 `@Tool` 注解标记方法：

```java
@Slf4j
@Service
public class MyTool {

    @Tool(name = "my_tool_function",
          description = "Description of what this tool does")
    public String myFunction(
            @ToolParam(description = "Parameter description") String param) {
        log.info("执行工具: {}", param);
        // 工具逻辑
        return "Result: " + param;
    }
}
```

### 2. 注册工具

在 `McpServerApplication.java` 中注册工具：

```java
@Bean
public ToolCallbackProvider myTools(MyTool myTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(myTool)
            .build();
}
```

### 3. 重启服务

工具会自动被 Spring AI MCP Server 发现和注册。

## 配置说明

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    mcp:
      server:
        name: spring-ai-mcp-demo-server    # MCP Server 名称
        version: 1.0.0                     # 版本号
        stdio: false                       # 使用 WebMVC，不使用 stdio
        protocol: STATELESS                # 协议模式：STATELESS 或 STATEFUL

server:
  port: 8081                               # HTTP 端口

logging:
  level:
    org.springframework.ai: DEBUG          # Spring AI 日志级别
```

## Spring AI MCP Server 工作原理

1. **工具定义**: 使用 `@Tool` 注解定义工具方法
2. **工具注册**: 通过 `ToolCallbackProvider` 注册工具
3. **自动发现**: Spring AI 自动扫描并注册所有工具
4. **MCP 端点**: Starter 自动创建标准的 MCP HTTP 端点
5. **协议处理**: 自动处理 MCP 协议的请求和响应

## 与 octopus-mcp-server 的对比

本项目参考了 [octopus-mcp-server](https://github.com/kanyun-inc/octopus-mcp-server) 的实现方式：

| 特性 | octopus-mcp-server | 本项目 |
|------|-------------------|--------|
| Spring AI 版本 | 1.1.0 | 1.1.0 |
| MCP Server Starter | ✅ | ✅ |
| @Tool 注解 | ✅ | ✅ |
| ToolCallbackProvider | ✅ | ✅ |
| 协议模式 | STATELESS | STATELESS |
| 业务场景 | Octopus 监控平台 | 通用计算器/天气 |

## 客户端集成

此 MCP Server 可以与任何支持 MCP 协议的客户端集成：

- **LangChain4j MCP Client**
- **Claude Desktop**
- **其他 MCP 兼容客户端**

客户端配置示例（根据具体客户端调整）：

```json
{
  "mcpServers": {
    "demo-server": {
      "url": "http://localhost:8081"
    }
  }
}
```

## 开发建议

1. **使用 @Tool 注解**: Spring AI 推荐的工具定义方式
2. **详细的描述**: 为工具和参数提供清晰的描述，帮助 AI 理解工具用途
3. **参数验证**: 在工具实现中验证参数有效性
4. **异常处理**: 合理处理异常，返回有意义的错误信息
5. **日志记录**: 记录工具调用的详细信息，便于调试

## 故障排查

### 服务无法启动

1. 检查 Java 版本（需要 Java 17+）
2. 检查端口 8081 是否被占用
3. 检查 Maven 依赖是否正确下载

### 工具未被注册

1. 确认工具类使用了 `@Service` 注解
2. 确认方法使用了 `@Tool` 注解
3. 确认在主应用类中注册了 `ToolCallbackProvider`
4. 查看启动日志，确认工具是否被扫描

### MCP 客户端连接失败

1. 确认服务已启动且运行正常
2. 检查网络连接和防火墙设置
3. 确认客户端配置的 URL 正确
4. 查看服务端日志，确认请求是否到达

## 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [MCP 协议规范](https://spec.modelcontextprotocol.io/)
- [octopus-mcp-server 项目](https://github.com/kanyun-inc/octopus-mcp-server)

## 许可证

MIT License
