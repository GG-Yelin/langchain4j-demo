# 工具调用方式对比

## 快速对比表

| 维度 | @Tool 注解 | MCP 协议 |
|------|-----------|---------|
| **本质** | 本地方法调用 | 远程服务调用 |
| **类比** | 调用 Java 方法 | 调用 HTTP API |
| **位置** | 同一进程内 | 独立进程/服务器 |
| **语言** | 必须 Java | 任意语言 |
| **通信** | 直接方法调用 | 网络/进程间通信 |
| **性能** | 极快（纳秒级） | 较慢（毫秒级） |
| **开销** | 几乎无 | 网络/序列化开销 |
| **部署** | 随应用打包 | 独立部署 |
| **扩展性** | 需要重新编译 | 动态添加/移除 |
| **隔离性** | 无隔离 | 进程/网络隔离 |
| **复杂度** | 极简 | 需要额外服务 |

## 代码对比

### @Tool 注解方式

```java
// 1. 定义工具（Java 类）
public class WeatherTool {
    @Tool("获取天气")
    public String getWeather(@P("城市") String city) {
        // 调用天气 API
        return "北京：晴，25°C";
    }
}

// 2. 使用工具
WeatherTool tool = new WeatherTool();

Assistant assistant = AiServices.builder(Assistant.class)
    .chatLanguageModel(chatModel)
    .tools(tool)  // 直接传入 Java 对象
    .build();

String result = assistant.chat("北京天气怎么样？");
// AI 自动调用 tool.getWeather("北京")
```

**特点**：
- ✅ 3 行代码搞定
- ✅ 类型安全
- ✅ IDE 自动补全
- ❌ 只能 Java

---

### MCP 方式

```java
// 1. 启动 MCP 服务器（Python）
// weather_mcp_server.py
from mcp.server import MCPServer

app = MCPServer(__name__)

@app.tool()
async def get_weather(city: str) -> str:
    """获取天气"""
    return f"{city}：晴，25°C"

app.run()  # 监听端口 3000

// 2. Java 客户端调用
@Service
public class McpService {
    private final McpClient mcpClient;

    public String chat(String message) {
        // 获取工具列表
        ListToolsResult tools = mcpClient.listTools();

        // 转换工具
        List<ToolSpecification> toolSpecs = convertTools(tools);

        // AI 决定调用
        ChatResponse response = chatModel.chat(
            ChatRequest.builder()
                .messages(UserMessage.from(message))
                .toolSpecifications(toolSpecs)
                .build()
        );

        // 执行工具调用
        if (response.aiMessage().hasToolExecutionRequests()) {
            for (ToolExecutionRequest req : response.aiMessage().toolExecutionRequests()) {
                // 通过 MCP 调用
                CallToolResult result = mcpClient.callTool(
                    req.name(),
                    parseArguments(req.arguments())
                );
                // 处理结果...
            }
        }

        return response.aiMessage().text();
    }
}
```

**特点**：
- ✅ 支持 Python/Node.js/Go 等任意语言
- ✅ 工具独立部署和扩展
- ✅ 进程隔离，更安全
- ❌ 代码复杂
- ❌ 需要额外服务
- ❌ 网络开销

## 架构对比图

### @Tool 注解架构

```
┌─────────────────────────────────────┐
│       同一个 JVM 进程                 │
│                                     │
│  ┌──────────┐      ┌─────────────┐ │
│  │          │      │             │ │
│  │ AI 服务   │─────►│ WeatherTool │ │
│  │          │ 直接  │ (Java 类)   │ │
│  │          │ 调用  │             │ │
│  └──────────┘      └─────────────┘ │
│                                     │
└─────────────────────────────────────┘

特点：
- 进程内调用
- 极快（纳秒级）
- 无网络开销
```

---

### MCP 架构

```
┌─────────────────┐         MCP          ┌──────────────────┐
│  Java 进程       │       Protocol       │  Python 进程      │
│                 │                      │                  │
│  ┌───────────┐  │   1. listTools()    │  ┌─────────────┐ │
│  │           │  │  ───────────────►    │  │             │ │
│  │ AI 服务    │  │   2. 工具列表        │  │ MCP Server  │ │
│  │           │  │  ◄───────────────    │  │             │ │
│  │           │  │                      │  │ @tool       │ │
│  │           │  │   3. callTool()     │  │ get_weather │ │
│  │           │  │  ───────────────►    │  │             │ │
│  │           │  │   4. 执行结果        │  │             │ │
│  │           │  │  ◄───────────────    │  │             │ │
│  └───────────┘  │                      │  └─────────────┘ │
│                 │                      │                  │
└─────────────────┘                      └──────────────────┘
     localhost                              localhost:3000

特点：
- 跨进程通信
- 较慢（毫秒级）
- 网络/序列化开销
- 语言无关
```

## 性能对比

### 延迟对比

```
@Tool 注解：
  方法调用: ~10 纳秒
  ├─ 参数准备: 5ns
  ├─ 方法执行: 3ns
  └─ 返回结果: 2ns

  总延迟: ~10 纳秒 ✅

---

MCP 协议（本地 HTTP）：
  工具调用: ~5-10 毫秒
  ├─ 序列化请求: 0.5ms
  ├─ HTTP 往返: 2-5ms
  ├─ 反序列化: 0.5ms
  ├─ 工具执行: 1ms
  └─ 结果返回: 1ms

  总延迟: ~5-10 毫秒 ⚠️

  比 @Tool 慢 500,000 - 1,000,000 倍！

---

MCP 协议（远程服务器）：
  工具调用: ~50-200 毫秒
  ├─ 序列化: 0.5ms
  ├─ 网络往返: 20-100ms
  ├─ 反序列化: 0.5ms
  ├─ 工具执行: 10ms
  └─ 结果返回: 20-80ms

  总延迟: ~50-200 毫秒 ❌

  比 @Tool 慢 5,000,000 - 20,000,000 倍！
```

### 吞吐量对比

```
场景：AI 需要调用 10 个工具

@Tool 注解：
  10 × 10ns = 100 纳秒
  可以处理: ~10,000,000 请求/秒

MCP（本地）：
  10 × 5ms = 50 毫秒
  可以处理: ~20 请求/秒

MCP（远程）：
  10 × 50ms = 500 毫秒
  可以处理: ~2 请求/秒
```

## 使用场景决策树

```
需要工具调用
    │
    ├─ 是 Java 代码可以实现的？
    │   └─ 是 → 使用 @Tool 注解 ✅
    │
    ├─ 需要用 Python/JS 库？
    │   └─ 是 → 使用 MCP ✅
    │
    ├─ 需要调用外部服务？
    │   └─ 是 → 使用 MCP ✅
    │
    ├─ 需要动态添加工具？
    │   └─ 是 → 使用 MCP ✅
    │
    ├─ 需要高性能（>1000 QPS）？
    │   └─ 是 → 使用 @Tool 注解 ✅
    │
    ├─ 需要安全隔离危险操作？
    │   └─ 是 → 使用 MCP ✅
    │
    └─ 不确定？
        └─ 先用 @Tool，需要时再迁移到 MCP
```

## 实际案例

### 案例1：计算器

**需求**：执行数学计算

**方案选择**：@Tool 注解 ✅

**原因**：
```java
// 简单、快速、无依赖
@Tool("计算")
public double calculate(String expression) {
    return engine.eval(expression);
}
```

---

### 案例2：数据分析

**需求**：使用 pandas 分析 CSV 数据

**方案选择**：MCP ✅

**原因**：
- pandas 是 Python 库
- Java 无法直接调用
- MCP 可以调用 Python 服务

```python
# Python MCP Server
@mcp_tool
def analyze_csv(file_path: str):
    df = pd.read_csv(file_path)
    return df.describe().to_json()
```

---

### 案例3：发送邮件

**需求**：通过企业邮件服务器发送邮件

**方案选择**：@Tool 注解 ✅

**原因**：
```java
// Java 有现成的邮件库
@Tool("发送邮件")
public void sendEmail(String to, String subject, String body) {
    javaMailSender.send(...);
}
```

---

### 案例4：网页爬虫

**需求**：爬取网页内容

**方案选择**：看情况

**@Tool**：
```java
// 如果用 Jsoup 就够了
@Tool("爬取网页")
public String scrape(String url) {
    return Jsoup.connect(url).get().text();
}
```

**MCP**：
```python
# 如果需要 Selenium 等高级功能
@mcp_tool
def scrape_dynamic(url: str):
    driver = webdriver.Chrome()
    driver.get(url)
    # 处理 JavaScript、等待加载等
    return driver.page_source
```

---

### 案例5：访问数据库

**需求**：查询数据库

**方案选择**：@Tool 注解 ✅

**原因**：
```java
// Spring 已经有数据库连接
@Tool("查询用户")
public User findUser(Long id) {
    return userRepository.findById(id).orElse(null);
}
```

除非：
- 数据库在另一个网络
- 需要通过专门的数据服务
- 那才考虑 MCP

---

### 案例6：Docker 操作

**需求**：管理 Docker 容器

**方案选择**：MCP ✅

**原因**：
- Docker CLI 工具
- 隔离危险操作
- 已有官方 MCP Server

```bash
# 使用官方 Docker MCP Server
npx @modelcontextprotocol/server-docker
```

## 混合使用

**最佳实践**：在同一个应用中混合使用两种方式！

```java
@Service
public class HybridToolService {

    // 本地工具（@Tool）
    private final CalculatorTool calculator = new CalculatorTool();
    private final DateTimeTool dateTime = new DateTimeTool();

    // 远程工具（MCP）
    private final McpClient mcpClient;

    public String chat(String message) {
        // 1. 收集本地工具
        List<Object> localTools = List.of(calculator, dateTime);

        // 2. 获取 MCP 远程工具
        List<ToolSpecification> remoteTools = mcpClient.listTools()
            .tools().stream()
            .map(this::convertToSpec)
            .collect(Collectors.toList());

        // 3. 合并所有工具
        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .tools(localTools.toArray())  // 本地工具
            // 远程工具需要在 ChatRequest 中指定
            .build();

        // AI 可以同时使用本地和远程工具！
        return assistant.chat(message);
    }
}
```

## 总结

### 核心理解

**MCP = AI 领域的 HTTP 协议**

| 对比 | Web 开发 | AI 开发 |
|------|---------|--------|
| **本地** | 调用本地函数 | @Tool 注解 |
| **远程** | 调用 HTTP API | MCP 协议 |
| **示例** | `user.getName()` vs `GET /api/user` | `tool.calculate()` vs `MCP callTool()` |

### 选择建议

**默认使用 @Tool 注解**，除非：
- ✅ 需要 Python/Node.js/Go 等其他语言
- ✅ 需要调用独立部署的服务
- ✅ 需要动态添加/移除工具
- ✅ 需要安全隔离
- ✅ 有现成的 MCP Server 可用

**性能敏感场景优先 @Tool**：
- 高频调用（>100 QPS）
- 低延迟要求（<10ms）
- 批量工具调用

**架构优先场景考虑 MCP**：
- 微服务架构
- 多语言技术栈
- 工具市场/插件系统
- 企业级安全要求
