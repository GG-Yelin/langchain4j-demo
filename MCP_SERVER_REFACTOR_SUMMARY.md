# MCP Server 重构总结

## 重构目标

基于 [octopus-mcp-server](https://github.com/kanyun-inc/octopus-mcp-server) 项目的实现方式，重新编写 `mcp-server` 模块，使用 Spring AI 官方的 MCP Server 实现。

## 核心改进

### 1. 使用 Spring AI 官方 MCP Server Starter

**之前的问题**：
- 尝试手动实现 MCP 协议
- 需要自己处理 JSON-RPC 请求/响应
- 需要手动管理 SSE 连接
- 协议兼容性和稳定性难以保证

**现在的解决方案**：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.1.0</version>
</dependency>
```

- ✅ 完全符合 MCP 协议规范
- ✅ 自动处理 HTTP/SSE 传输
- ✅ 开箱即用，无需手动实现
- ✅ 与 Spring AI 生态无缝集成

### 2. 基于注解的工具定义

参考 octopus-mcp-server 的实现方式：

```java
@Service
public class CalculatorTool {

    @Tool(name = "calculator_add",
        description = """
        A tool for adding two numbers together.
        This tool performs basic addition operation and returns the sum of two numbers.
        """)
    public double add(
            @ToolParam(description = "First number to add") double a,
            @ToolParam(description = "Second number to add") double b) {
        log.info("Calculator Tool - Add: {} + {}", a, b);
        double result = a + b;
        log.info("Result: {}", result);
        return result;
    }
}
```

**关键特性**：
- 使用 `@Tool` 注解定义工具方法
- 使用 `@ToolParam` 注解定义参数
- 支持多行文本块（`"""..."""`）编写详细描述
- 自动类型转换和参数验证

### 3. 工具注册机制

参考 octopus-mcp-server 的注册方式：

```java
@Bean
public ToolCallbackProvider calculatorTools(CalculatorTool calculatorTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(calculatorTool)
            .build();
}
```

**优势**：
- 通过 `MethodToolCallbackProvider` 自动注册
- Spring 依赖注入管理工具生命周期
- 支持多个工具类独立注册
- 自动工具发现和元数据提取

### 4. 配置优化

参考 octopus-mcp-server 的配置：

```yaml
spring:
  ai:
    mcp:
      server:
        name: langchain4j-demo-mcp-server
        version: 1.0.0
        stdio: false          # 使用 WebMVC 而非 stdio
        protocol: STATELESS   # 无状态协议
```

**配置说明**：
- `stdio: false` - 使用 HTTP/SSE 传输而非标准输入输出
- `protocol: STATELESS` - 每个请求独立处理，不保持连接状态

## 实现细节

### 项目结构

```
mcp-server/
├── pom.xml                                    # Maven 配置
├── README.md                                  # 项目文档
└── src/main/
    ├── java/org/example/mcpserver/
    │   ├── McpServerApplication.java         # 主应用类
    │   └── tools/
    │       ├── CalculatorTool.java           # 计算器工具
    │       └── WeatherTool.java              # 天气工具
    └── resources/
        └── application.yml                    # 配置文件
```

### 已实现的工具

#### 1. CalculatorTool（计算器工具）

包含 6 个工具方法：
- `calculator_add` - 加法
- `calculator_subtract` - 减法
- `calculator_multiply` - 乘法
- `calculator_divide` - 除法（带除零检查）
- `calculator_power` - 幂运算
- `calculator_sqrt` - 平方根（带负数检查）

**改进点**：
- 详细的多行描述
- 完善的参数验证
- 清晰的日志输出
- 有意义的错误消息

#### 2. WeatherTool（天气工具）

包含 2 个工具方法：
- `weather_get_current` - 查询当前天气
- `weather_get_forecast` - 查询天气预报（1-7天）

**改进点**：
- 支持中英文城市名
- 参数范围验证
- 空值检查
- 详细的日志记录

### 代码质量改进

#### 1. 详细的描述文本

```java
@Tool(name = "calculator_divide",
    description = """
    A tool for dividing first number by second number.
    This tool performs basic division operation.
    The divisor cannot be zero, otherwise an error will be thrown.
    """)
```

参考 octopus-mcp-server 使用多行文本块，为 AI 提供清晰的工具说明。

#### 2. 参数验证

```java
if (b == 0) {
    log.error("Division by zero error: divisor = {}", b);
    throw new IllegalArgumentException("Divisor cannot be zero. Please provide a non-zero divisor.");
}
```

参考 octopus-mcp-server 的验证方式，对所有可能的错误情况进行检查。

#### 3. 日志记录

```java
log.info("Calculator Tool - Add: {} + {}", a, b);
double result = a + b;
log.info("Result: {}", result);
```

参考 octopus-mcp-server 的日志格式，记录完整的调用信息。

## 与 octopus-mcp-server 的对比

| 方面 | octopus-mcp-server | 本项目 |
|------|-------------------|--------|
| **Spring Boot** | 3.3.6 | 3.4.1 |
| **Spring AI** | 1.1.0 | 1.1.0 |
| **Java 版本** | 21 | 17 |
| **MCP Starter** | spring-ai-starter-mcp-server-webmvc | spring-ai-starter-mcp-server-webmvc |
| **传输方式** | WebMVC (HTTP/SSE) | WebMVC (HTTP/SSE) |
| **协议模式** | STATELESS | STATELESS |
| **工具注解** | @Tool + @ToolParam | @Tool + @ToolParam |
| **工具注册** | MethodToolCallbackProvider | MethodToolCallbackProvider |
| **业务领域** | Octopus 监控平台 | 通用计算器/天气 |

### 相同点

1. ✅ 都使用 Spring AI MCP Server WebMVC Starter
2. ✅ 都使用 `@Tool` 和 `@ToolParam` 注解
3. ✅ 都使用 `MethodToolCallbackProvider` 注册工具
4. ✅ 都使用 STATELESS 协议模式
5. ✅ 都采用 HTTP/SSE 传输方式

### 差异点

1. **Java 版本**: octopus 使用 Java 21，本项目使用 Java 17（更广泛兼容）
2. **业务场景**: octopus 专注于 Octopus 监控平台，本项目是通用示例
3. **工具复杂度**: octopus 工具涉及复杂的 API 调用，本项目是简单示例

## 学习要点

从 octopus-mcp-server 学到的关键实现模式：

### 1. 工具定义模式

```java
@Service
public class Tool {
    @Tool(name = "tool_name",
          description = """多行描述""")
    public ReturnType method(
        @ToolParam(description = "参数说明") Type param) {
        // 实现
    }
}
```

### 2. 工具注册模式

```java
@Bean
public ToolCallbackProvider xxxTools(XxxTool tool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(tool)
            .build();
}
```

### 3. 参数验证模式

```java
if (invalidCondition) {
    log.error("Error description: {}", value);
    throw new IllegalArgumentException("Clear error message");
}
```

### 4. 配置模式

```yaml
spring:
  ai:
    mcp:
      server:
        name: server-name
        version: x.x.x
        stdio: false
        protocol: STATELESS
```

## 测试方式

### 1. 启动服务

```bash
cd mcp-server
mvn spring-boot:run
```

### 2. 运行测试脚本

```bash
./test-mcp-server.sh
```

测试脚本会验证：
- ✅ 服务健康检查
- ✅ 工具列表获取
- ✅ 计算器工具调用
- ✅ 天气工具调用

### 3. 手动测试

使用 curl 测试工具列表：

```bash
curl -X POST http://localhost:8081/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "tools/list"
  }'
```

## 后续扩展

### 添加新工具的步骤

参考 octopus-mcp-server 的模式：

1. **创建工具类**

```java
@Service
public class NewTool {
    @Tool(name = "new_tool", description = "...")
    public Result method(@ToolParam(...) Type param) {
        // 实现
    }
}
```

2. **注册工具**

```java
@Bean
public ToolCallbackProvider newTools(NewTool tool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(tool)
            .build();
}
```

3. **重启服务** - 工具自动生效

## 总结

通过学习 octopus-mcp-server 项目，我们成功重构了 mcp-server 模块：

### 核心成果

1. ✅ 使用 Spring AI 官方 MCP Server 实现
2. ✅ 遵循 octopus-mcp-server 的最佳实践
3. ✅ 完全符合 MCP 协议规范
4. ✅ 代码清晰、可维护性高
5. ✅ 易于扩展新工具

### 关键优势

- **标准化**: 基于 Spring AI 官方实现，符合标准
- **简单化**: 使用注解，无需手动处理协议细节
- **可靠性**: 经过验证的生产级实现
- **可扩展**: 添加新工具只需简单配置

### 参考资源

- ✅ [octopus-mcp-server 项目](https://github.com/kanyun-inc/octopus-mcp-server)
- ✅ [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- ✅ [MCP 协议规范](https://spec.modelcontextprotocol.io/)

## 致谢

感谢 octopus-mcp-server 项目提供了优秀的参考实现，使我们能够快速理解和实践 Spring AI MCP Server 的正确使用方式。
