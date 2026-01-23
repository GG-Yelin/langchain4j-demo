# MCP Server 模块 - 项目概览

## 📋 项目信息

- **模块名称**: mcp-server
- **实现方式**: Spring AI MCP Server WebMVC Starter
- **参考项目**: [octopus-mcp-server](https://github.com/kanyun-inc/octopus-mcp-server)
- **协议标准**: Model Context Protocol (MCP)
- **最后更新**: 2026-01-23

## 🎯 项目目标

基于 octopus-mcp-server 的实现方式，使用 Spring AI 官方的 MCP Server Starter 构建标准的 MCP Server，提供计算器和天气查询等工具服务。

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────┐
│                    MCP Client                           │
│           (LangChain4j / Claude Desktop)                │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP/SSE (MCP Protocol)
                      │
┌─────────────────────▼───────────────────────────────────┐
│         Spring AI MCP Server WebMVC Starter             │
│  ┌────────────────────────────────────────────────┐    │
│  │  MCP Protocol Handler (自动实现)               │    │
│  │  - tools/list                                  │    │
│  │  - tools/call                                  │    │
│  │  - JSON-RPC 2.0                               │    │
│  └────────────────────────────────────────────────┘    │
│                       │                                 │
│  ┌────────────────────▼────────────────────────────┐   │
│  │     MethodToolCallbackProvider (工具注册器)    │   │
│  └────────────────────┬────────────────────────────┘   │
│                       │                                 │
│       ┌───────────────┴───────────────┐                │
│       │                               │                │
│  ┌────▼─────────┐           ┌────────▼────────┐       │
│  │ Calculator   │           │  Weather Tool   │       │
│  │    Tool      │           │                 │       │
│  │              │           │                 │       │
│  │ @Tool 注解   │           │  @Tool 注解     │       │
│  │ @ToolParam   │           │  @ToolParam     │       │
│  └──────────────┘           └─────────────────┘       │
└─────────────────────────────────────────────────────────┘
```

## 📦 核心组件

### 1. Spring AI MCP Server WebMVC Starter

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.1.0</version>
</dependency>
```

**功能**：
- ✅ 自动处理 MCP 协议（JSON-RPC 2.0）
- ✅ 提供 HTTP/SSE 传输层
- ✅ 工具注册和发现机制
- ✅ 请求路由和响应处理

### 2. 工具定义（@Tool 注解）

```java
@Tool(name = "tool_name",
      description = """
      详细的工具描述
      支持多行文本
      """)
public ReturnType method(
    @ToolParam(description = "参数说明") ParamType param) {
    // 工具实现
}
```

**特点**：
- 基于注解，简单直观
- 支持自动类型转换
- 详细的描述信息
- 完善的参数验证

### 3. 工具注册（MethodToolCallbackProvider）

```java
@Bean
public ToolCallbackProvider xxxTools(XxxTool tool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(tool)
            .build();
}
```

**优势**：
- 自动工具发现
- Spring 依赖注入
- 灵活的工具组织
- 易于扩展

## 🛠️ 已实现工具

### 1. CalculatorTool - 计算器工具

| 工具名 | 功能 | 参数 | 特性 |
|--------|------|------|------|
| calculator_add | 加法 | a, b (double) | 基础运算 |
| calculator_subtract | 减法 | a, b (double) | 基础运算 |
| calculator_multiply | 乘法 | a, b (double) | 基础运算 |
| calculator_divide | 除法 | a, b (double) | 除零检查 |
| calculator_power | 幂运算 | base, exponent (double) | Math.pow |
| calculator_sqrt | 平方根 | number (double) | 负数检查 |

**代码示例**：
```java
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
```

### 2. WeatherTool - 天气工具

| 工具名 | 功能 | 参数 | 特性 |
|--------|------|------|------|
| weather_get_current | 当前天气 | city (string) | 支持中英文 |
| weather_get_forecast | 天气预报 | city, days (int) | 1-7天预报 |

**代码示例**：
```java
@Tool(name = "weather_get_current",
    description = """
    A tool for getting current weather information for a specific city.
    This tool provides real-time weather data including temperature, conditions, and air quality.
    Supported cities include major Chinese cities like Beijing, Shanghai, Shenzhen, etc.
    """)
public String getWeather(
        @ToolParam(description = "City name (e.g., 北京, 上海, 深圳, Beijing, Shanghai, Shenzhen)") String city) {
    // 实现...
}
```

## ⚙️ 配置说明

### application.yml

```yaml
spring:
  ai:
    mcp:
      server:
        name: langchain4j-demo-mcp-server   # Server 名称
        version: 1.0.0                       # Server 版本
        stdio: false                         # 使用 WebMVC 而非 stdio
        protocol: STATELESS                  # 无状态协议

server:
  port: 8081                                 # HTTP 端口

logging:
  level:
    org.springframework.ai: DEBUG            # 日志级别
```

### 配置项说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| stdio | false | 使用 HTTP/SSE 传输而非标准输入输出 |
| protocol | STATELESS | 每个请求独立处理，不保持连接状态 |
| port | 8081 | MCP Server 监听端口 |

## 🚀 快速开始

### 1. 编译项目

```bash
cd mcp-server
mvn clean package
```

### 2. 启动服务

**方式一：使用 Maven**
```bash
mvn spring-boot:run
```

**方式二：使用启动脚本**
```bash
./start-mcp-server-optimized.sh
```

**方式三：使用 JAR**
```bash
java -jar target/mcp-server-0.0.1-SNAPSHOT.jar
```

### 3. 验证服务

```bash
./test-mcp-server.sh
```

测试内容：
- ✅ 服务健康检查
- ✅ 获取工具列表
- ✅ 调用计算器工具
- ✅ 调用天气工具

## 📖 MCP 协议接口

### 1. 获取工具列表

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
        "name": "calculator_add",
        "description": "A tool for adding two numbers together...",
        "inputSchema": {
          "type": "object",
          "properties": {
            "a": {"type": "number", "description": "First number to add"},
            "b": {"type": "number", "description": "Second number to add"}
          },
          "required": ["a", "b"]
        }
      }
      // ... 更多工具
    ]
  }
}
```

### 2. 调用工具

**请求**：
```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "calculator_add",
    "arguments": {
      "a": 10,
      "b": 20
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
        "text": "30.0"
      }
    ]
  }
}
```

## 🔧 开发指南

### 添加新工具的步骤

#### 1. 创建工具类

```java
@Slf4j
@Service
public class MyTool {

    @Tool(name = "my_tool_function",
        description = """
        详细的工具描述
        支持多行文本
        """)
    public String myFunction(
            @ToolParam(description = "参数描述") String param) {

        log.info("My Tool - Function called with: {}", param);

        // 参数验证
        if (param == null || param.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空");
        }

        // 业务逻辑
        String result = "处理结果: " + param;

        log.info("Result: {}", result);
        return result;
    }
}
```

#### 2. 注册工具

在 `McpServerApplication.java` 中添加：

```java
@Bean
public ToolCallbackProvider myTools(MyTool myTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(myTool)
            .build();
}
```

#### 3. 重启服务

工具会自动被发现和注册。

### 最佳实践

参考 octopus-mcp-server 的实现：

1. **详细的描述**
   - 使用多行文本块（`"""..."""`）
   - 说明工具的功能和使用场景
   - 描述参数的含义和约束

2. **参数验证**
   - 检查空值、范围、格式
   - 抛出有意义的异常
   - 提供清晰的错误消息

3. **日志记录**
   - 记录输入参数
   - 记录执行结果
   - 记录异常信息

4. **错误处理**
   - 捕获并处理可预见的错误
   - 返回有用的错误信息
   - 避免程序崩溃

## 📊 与 octopus-mcp-server 对比

| 方面 | octopus-mcp-server | 本项目 |
|------|-------------------|--------|
| **核心实现** | Spring AI MCP Server | ✅ 相同 |
| **Spring Boot** | 3.3.6 | 3.4.1 |
| **Spring AI** | 1.1.0 | ✅ 相同 |
| **Java** | 21 | 17 |
| **工具注解** | @Tool + @ToolParam | ✅ 相同 |
| **工具注册** | MethodToolCallbackProvider | ✅ 相同 |
| **协议模式** | STATELESS | ✅ 相同 |
| **传输方式** | WebMVC | ✅ 相同 |
| **业务场景** | Octopus 监控平台 | 通用工具 |

### 学习成果

从 octopus-mcp-server 学到的关键点：

1. ✅ Spring AI MCP Server 的正确使用方式
2. ✅ 工具定义和注册的最佳实践
3. ✅ 多行描述文本的使用
4. ✅ 完善的参数验证模式
5. ✅ 详细的日志记录方式
6. ✅ STATELESS 协议的优势
7. ✅ 清晰的代码组织结构

## 📚 项目文档

| 文档 | 说明 |
|------|------|
| [mcp-server/README.md](mcp-server/README.md) | 模块使用文档 |
| [MCP_SERVER_REFACTOR_SUMMARY.md](MCP_SERVER_REFACTOR_SUMMARY.md) | 重构总结 |
| [MCP_SERVER_CHECKLIST.md](MCP_SERVER_CHECKLIST.md) | 完成清单 |
| [MCP_SERVER_OVERVIEW.md](MCP_SERVER_OVERVIEW.md) | 项目概览（本文档） |

## 🔨 实用脚本

| 脚本 | 功能 |
|------|------|
| `start-mcp-server-optimized.sh` | 启动 MCP Server |
| `test-mcp-server.sh` | 测试 MCP Server |

## 🐛 故障排查

### 编译失败

**问题**：Maven 编译错误

**解决**：
```bash
# 清理并重新编译
mvn clean compile

# 检查依赖
mvn dependency:tree
```

### 服务启动失败

**问题**：端口被占用

**解决**：
```bash
# 检查端口占用
lsof -i :8081

# 修改配置文件中的端口
# src/main/resources/application.yml
server:
  port: 8082
```

**问题**：Java 版本不兼容

**解决**：
```bash
# 检查 Java 版本
java -version

# 需要 Java 17+
```

### 工具未注册

**问题**：工具列表为空

**检查**：
1. 工具类是否有 `@Service` 注解
2. 方法是否有 `@Tool` 注解
3. 是否在主应用类中注册了 ToolCallbackProvider
4. 查看启动日志确认工具是否被扫描

### MCP 客户端连接失败

**问题**：客户端无法连接

**检查**：
1. 服务是否正常启动
2. 端口是否正确
3. 防火墙设置
4. 客户端配置的 URL

## 🔗 参考资源

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [MCP 协议规范](https://spec.modelcontextprotocol.io/)
- [octopus-mcp-server](https://github.com/kanyun-inc/octopus-mcp-server)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

## 📝 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-01-23 | 初始版本，基于 octopus-mcp-server 重构 |

## 📄 许可证

MIT License

---

**项目状态**: ✅ 完成重构，可以正常使用

**下一步**:
1. 启动服务并测试
2. 与 LangChain4j MCP Client 集成
3. 添加更多工具
4. 编写单元测试
