# LangChain4j 工具调用方式完整指南

LangChain4j 支持多种工具调用方式，每种方式适用于不同的场景。

## 1. 内置工具（Built-in Tools）

### 方式1：使用 @Tool 注解定义工具类

这是**最常用、最推荐**的方式。

#### 示例：定义工具类

```java
/**
 * 计算器工具
 */
public class CalculatorTool {

    @Tool("计算两个数字的和")
    public double add(
        @P("第一个数字") double a,
        @P("第二个数字") double b
    ) {
        return a + b;
    }

    @Tool("计算两个数字的乘积")
    public double multiply(double a, double b) {
        return a * b;
    }

    @Tool("计算平方根")
    public double sqrt(@P("要计算平方根的数字") double number) {
        if (number < 0) {
            throw new IllegalArgumentException("不能计算负数的平方根");
        }
        return Math.sqrt(number);
    }
}

/**
 * 天气工具
 */
public class WeatherTool {

    @Tool("获取指定城市的当前天气")
    public String getCurrentWeather(
        @P("城市名称，如：北京、上海") String city
    ) {
        // 实际应用中调用天气 API
        return "北京当前天气：晴，温度 25°C，湿度 60%";
    }

    @Tool("获取未来几天的天气预报")
    public String getWeatherForecast(
        @P("城市名称") String city,
        @P("预报天数，1-7天") int days
    ) {
        return String.format("%s 未来 %d 天天气预报...", city, days);
    }
}
```

#### 使用工具

```java
@Service
@RequiredArgsConstructor
public class ToolChatService {

    private final OpenAiChatModel chatModel;

    public String chatWithTools(String userMessage) {
        // 创建工具实例
        CalculatorTool calculator = new CalculatorTool();
        WeatherTool weather = new WeatherTool();

        // 使用 AiServices 注入工具
        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .tools(calculator, weather)  // 注入多个工具
            .build();

        return assistant.chat(userMessage);
    }

    interface Assistant {
        String chat(String message);
    }
}
```

#### 完整示例（用户询问）

```java
// 用户：计算 25 + 17 的结果
// AI 自动调用 CalculatorTool.add(25, 17)
// 返回：42

// 用户：北京今天天气怎么样？
// AI 自动调用 WeatherTool.getCurrentWeather("北京")
// 返回：北京当前天气：晴，温度 25°C，湿度 60%
```

---

## 2. Spring Bean 工具

将工具注册为 Spring Bean，由 Spring 管理生命周期。

### 示例

```java
@Component
public class DatabaseTool {

    @Autowired
    private UserRepository userRepository;

    @Tool("根据用户ID查询用户信息")
    public String getUserInfo(@P("用户ID") Long userId) {
        User user = userRepository.findById(userId)
            .orElse(null);
        if (user == null) {
            return "用户不存在";
        }
        return String.format("用户：%s，邮箱：%s", user.getName(), user.getEmail());
    }

    @Tool("搜索用户")
    public List<String> searchUsers(@P("搜索关键词") String keyword) {
        return userRepository.findByNameContaining(keyword)
            .stream()
            .map(User::getName)
            .collect(Collectors.toList());
    }
}

@Service
@RequiredArgsConstructor
public class AIServiceWithSpringTools {

    private final OpenAiChatModel chatModel;
    private final ApplicationContext applicationContext;

    public String chat(String message) {
        // 获取所有带 @Tool 方法的 Spring Bean
        Map<String, Object> tools = new HashMap<>();
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            // 检查 bean 是否包含 @Tool 注解的方法
            if (hasToolMethods(bean)) {
                tools.put(beanName, bean);
            }
        }

        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .tools(tools.values().toArray())
            .build();

        return assistant.chat(message);
    }
}
```

---

## 3. 动态工具（ToolSpecification）

手动定义工具规范，不使用注解。适合运行时动态创建工具。

### 示例

```java
public class DynamicToolExample {

    public String chatWithDynamicTools(String userMessage) {
        // 手动定义工具规范
        ToolSpecification weatherTool = ToolSpecification.builder()
            .name("get_weather")
            .description("获取指定城市的天气信息")
            .addParameter("city", JsonSchemaProperty.STRING,
                JsonSchemaProperty.description("城市名称，如：北京、上海"))
            .addParameter("unit", JsonSchemaProperty.STRING,
                JsonSchemaProperty.description("温度单位：celsius 或 fahrenheit"),
                JsonSchemaProperty.enums("celsius", "fahrenheit"))
            .build();

        ToolSpecification calculatorTool = ToolSpecification.builder()
            .name("calculate")
            .description("执行数学计算")
            .addParameter("expression", JsonSchemaProperty.STRING,
                JsonSchemaProperty.description("数学表达式，如：2+2, 10*5"))
            .build();

        // 构建请求
        ChatRequest request = ChatRequest.builder()
            .messages(UserMessage.from(userMessage))
            .toolSpecifications(weatherTool, calculatorTool)
            .build();

        ChatResponse response = chatModel.chat(request);

        // 处理工具调用
        if (response.aiMessage().hasToolExecutionRequests()) {
            for (ToolExecutionRequest toolRequest : response.aiMessage().toolExecutionRequests()) {
                String toolName = toolRequest.name();
                String arguments = toolRequest.arguments();

                // 根据工具名称执行相应的逻辑
                String result = executeToolCall(toolName, arguments);

                // 将结果返回给 AI
                ToolExecutionResultMessage resultMessage = ToolExecutionResultMessage.from(
                    toolRequest, result);

                // 继续对话...
            }
        }

        return response.aiMessage().text();
    }

    private String executeToolCall(String toolName, String arguments) {
        // 解析参数并执行工具
        JsonObject args = JsonParser.parseString(arguments).getAsJsonObject();

        switch (toolName) {
            case "get_weather":
                String city = args.get("city").getAsString();
                return getWeather(city);
            case "calculate":
                String expression = args.get("expression").getAsString();
                return calculate(expression);
            default:
                return "未知工具";
        }
    }
}
```

---

## 4. MCP (Model Context Protocol)

通过标准协议调用外部工具服务器。

### 特点
- 标准化协议
- 支持远程工具
- 工具服务独立部署
- 支持多种语言实现工具服务器

### 示例（你的项目已有）

```java
@Service
@RequiredArgsConstructor
public class McpService {

    private final McpClient mcpClient;
    private final OpenAiChatModel chatModel;

    public String chatWithMcpTools(String userMessage) {
        // 1. 获取 MCP 工具列表
        List<Tool> tools = mcpClient.listTools().tools();

        // 2. 转换为 LangChain4j 工具规范
        List<ToolSpecification> toolSpecs = tools.stream()
            .map(this::convertToToolSpec)
            .collect(Collectors.toList());

        // 3. 发送请求
        ChatRequest request = ChatRequest.builder()
            .messages(UserMessage.from(userMessage))
            .toolSpecifications(toolSpecs)
            .build();

        ChatResponse response = chatModel.chat(request);

        // 4. 处理工具调用请求
        if (response.aiMessage().hasToolExecutionRequests()) {
            for (ToolExecutionRequest req : response.aiMessage().toolExecutionRequests()) {
                // 通过 MCP 协议调用工具
                CallToolResult result = mcpClient.callTool(
                    req.name(),
                    parseArguments(req.arguments())
                );
                // ... 处理结果
            }
        }

        return response.aiMessage().text();
    }
}
```

---

## 5. OpenAI Function Calling（原生方式）

直接使用 OpenAI 的 Function Calling 特性。

### 示例

```java
public class OpenAIFunctionCallingExample {

    public String chatWithFunctions(String userMessage) {
        // 定义函数
        JsonObject functionDef = new JsonObject();
        functionDef.addProperty("name", "get_current_weather");
        functionDef.addProperty("description", "获取指定城市的当前天气");

        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        JsonObject cityProp = new JsonObject();
        cityProp.addProperty("type", "string");
        cityProp.addProperty("description", "城市名称");
        properties.add("city", cityProp);

        parameters.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("city");
        parameters.add("required", required);

        functionDef.add("parameters", parameters);

        // 发送请求
        ChatRequest request = ChatRequest.builder()
            .messages(UserMessage.from(userMessage))
            .parameters(ChatRequestParameters.builder()
                .functions(List.of(functionDef))
                .build())
            .build();

        ChatResponse response = chatModel.chat(request);

        // 处理函数调用
        // ...

        return response.aiMessage().text();
    }
}
```

---

## 6. LangChain Tools（兼容 LangChain）

使用兼容 Python LangChain 的工具定义。

### 示例

```java
// 使用 LangChain4j 提供的预定义工具
import dev.langchain4j.agent.tool.langchain4j.*;

public class LangChainToolsExample {

    public void usePrebuiltTools() {
        // 使用内置的工具
        Calculator calculator = new Calculator();
        WebSearch webSearch = new WebSearch(apiKey);
        WikipediaSearch wikipedia = new WikipediaSearch();

        Assistant assistant = AiServices.builder(Assistant.class)
            .chatLanguageModel(chatModel)
            .tools(calculator, webSearch, wikipedia)
            .build();
    }
}
```

---

## 工具调用方式对比

| 方式 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **@Tool 注解** | 简单易用、类型安全、IDE支持 | 需要重新编译 | 内部业务逻辑 |
| **Spring Bean** | 依赖注入、生命周期管理 | 依赖Spring | 企业应用 |
| **动态工具** | 运行时动态创建、灵活 | 代码复杂 | 插件系统 |
| **MCP** | 标准化、语言无关、远程调用 | 需要额外服务 | 微服务架构 |
| **OpenAI原生** | 直接控制、底层访问 | 繁琐、易错 | 特殊需求 |
| **LangChain兼容** | 复用现有工具 | 依赖特定库 | 迁移项目 |

---

## 推荐的最佳实践

### 1. 简单应用
使用 `@Tool` 注解：

```java
public class MyTools {
    @Tool("工具描述")
    public String myTool(@P("参数描述") String param) {
        return "结果";
    }
}
```

### 2. 企业应用
使用 Spring Bean：

```java
@Component
public class BusinessTool {
    @Autowired
    private SomeService service;

    @Tool("业务工具")
    public String doSomething(String input) {
        return service.process(input);
    }
}
```

### 3. 微服务架构
使用 MCP：

```
[AI Service] <--MCP--> [Tool Server 1: Python]
                    <--MCP--> [Tool Server 2: Node.js]
                    <--MCP--> [Tool Server 3: Java]
```

### 4. 插件系统
使用动态工具：

```java
// 从配置文件或数据库加载工具定义
List<ToolSpecification> tools = loadToolsFromConfig();
```

---

## 工具执行流程

```
1. 用户输入
   ↓
2. AI 分析是否需要工具
   ↓
3. AI 决定调用哪个工具
   ↓
4. 生成工具调用请求（包含参数）
   ↓
5. LangChain4j 执行工具
   ↓
6. 工具返回结果
   ↓
7. AI 综合结果生成回复
   ↓
8. 返回给用户
```

---

## 示例：完整的工具调用实现

```java
/**
 * 综合工具服务示例
 */
@Service
@RequiredArgsConstructor
public class ComprehensiveToolService {

    private final OpenAiChatModel chatModel;

    // 工具类
    public static class MathTools {
        @Tool("执行数学计算")
        public double calculate(
            @P("数学表达式，如：2+2, 10*5") String expression
        ) {
            // 使用 JavaScript 引擎或其他方式计算
            ScriptEngine engine = new ScriptEngineManager()
                .getEngineByName("JavaScript");
            try {
                return ((Number) engine.eval(expression)).doubleValue();
            } catch (Exception e) {
                throw new RuntimeException("计算错误: " + e.getMessage());
            }
        }
    }

    public static class DateTimeTools {
        @Tool("获取当前日期时间")
        public String getCurrentDateTime() {
            return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Tool("计算日期差")
        public long daysBetween(
            @P("开始日期，格式：yyyy-MM-dd") String startDate,
            @P("结束日期，格式：yyyy-MM-dd") String endDate
        ) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return ChronoUnit.DAYS.between(start, end);
        }
    }

    public static class TextTools {
        @Tool("统计文本字数")
        public int countWords(@P("要统计的文本") String text) {
            return text.split("\\s+").length;
        }

        @Tool("文本反转")
        public String reverseText(@P("要反转的文本") String text) {
            return new StringBuilder(text).reverse().toString();
        }
    }

    /**
     * 使用所有工具进行聊天
     */
    public String chatWithAllTools(String userMessage) {
        // 创建工具实例
        MathTools math = new MathTools();
        DateTimeTools dateTime = new DateTimeTools();
        TextTools text = new TextTools();

        // 构建 AI 助手
        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
            .chatLanguageModel(chatModel)
            .tools(math, dateTime, text)  // 注入所有工具
            .build();

        return assistant.chat(userMessage);
    }

    interface ToolAssistant {
        String chat(String message);
    }
}
```

---

## 测试示例

```java
@Test
public void testToolCalling() {
    // 测试数学计算
    String result1 = service.chatWithAllTools("计算 (25 + 17) * 3 的结果");
    // AI 会调用 calculate("(25 + 17) * 3")
    // 返回：126

    // 测试日期时间
    String result2 = service.chatWithAllTools("现在几点了？");
    // AI 会调用 getCurrentDateTime()
    // 返回：当前时间是 2026-01-13 18:50:00

    // 测试文本处理
    String result3 = service.chatWithAllTools("'Hello World' 有几个单词？");
    // AI 会调用 countWords("Hello World")
    // 返回：2个单词

    // 测试多工具组合
    String result4 = service.chatWithAllTools(
        "从今天到2026年春节还有多少天？如果反过来写日期是什么？"
    );
    // AI 可能会调用：
    // 1. getCurrentDateTime() 获取今天日期
    // 2. daysBetween("2026-01-13", "2026-01-29") 计算天数
    // 3. reverseText("2026-01-29") 反转日期
}
```

这就是 LangChain4j 的所有主要工具调用方式！每种方式都有其适用场景，你可以根据实际需求选择。
