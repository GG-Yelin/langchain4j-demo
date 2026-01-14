# 使用 BuiltInTools 的工具调用接口

## 概述

`/api/tool/chat` 接口现在直接使用 `BuiltInTools` 中定义的 `@Tool` 注解工具，无需重新定义工具规范。

## 核心优势

### 使用 @Tool 注解的好处

1. **代码简洁**：工具定义和实现在一起，无需手动管理工具规范
2. **自动执行**：LangChain4j 自动处理工具调用，无需手动执行
3. **类型安全**：编译时检查参数类型
4. **易于维护**：工具代码集中在 `BuiltInTools` 类中

### 与 ToolSpecification 的对比

```java
// ❌ ToolSpecification 方式（繁琐）
ToolSpecification spec = ToolSpecification.builder()
    .name("add")
    .description("计算两个数的和")
    .parameters(JsonObjectSchema.builder()
        .addNumberProperty("a", "第一个数字")
        .addNumberProperty("b", "第二个数字")
        .required("a", "b")
        .build())
    .build();

// 还需要手动实现工具执行逻辑
String result = executeToolFunction(name, args);

// ✅ @Tool 注解方式（简洁）
@Tool("计算两个数的和")
public double add(@P("第一个数字") double a, @P("第二个数字") double b) {
    return a + b;
}

// 工具自动执行，无需额外代码
```

## 可用工具

### 1. Calculator（计算器）

```java
public static class Calculator {
    @Tool("执行数学计算表达式")
    String calculate(String expression)

    @Tool("计算两个数字的和")
    double add(double a, double b)

    @Tool("计算两个数字的差")
    double subtract(double a, double b)

    @Tool("计算两个数字的乘积")
    double multiply(double a, double b)

    @Tool("计算两个数字的商")
    double divide(double a, double b)

    @Tool("计算平方根")
    double sqrt(double number)

    @Tool("计算幂运算")
    double power(double base, double exponent)
}
```

### 2. DateTime（日期时间）

```java
public static class DateTime {
    @Tool("获取当前日期时间")
    String getCurrentDateTime()

    @Tool("获取当前日期")
    String getCurrentDate()

    @Tool("计算两个日期之间的天数")
    long daysBetween(String startDate, String endDate)

    @Tool("判断日期是星期几")
    String getDayOfWeek(String date)

    @Tool("在指定日期上增加天数")
    String addDays(String date, int days)

    @Tool("格式化日期")
    String formatDate(String date, String pattern)
}
```

### 3. TextProcessor（文本处理）

```java
public static class TextProcessor {
    @Tool("计算文本中的单词数量")
    int countWords(String text)

    @Tool("将文本转换为大写")
    String toUpperCase(String text)

    @Tool("将文本转换为小写")
    String toLowerCase(String text)

    @Tool("反转文本")
    String reverse(String text)

    @Tool("移除文本中的空格")
    String removeSpaces(String text)

    @Tool("计算文本长度")
    int length(String text)

    @Tool("检查文本是否包含子串")
    boolean contains(String text, String substring)
}
```

### 4. RandomGenerator（随机数生成）

```java
public static class RandomGenerator {
    @Tool("生成指定范围内的随机整数")
    int randomInt(int min, int max)

    @Tool("生成0到1之间的随机浮点数")
    double randomDouble()

    @Tool("生成随机布尔值")
    boolean randomBoolean()

    @Tool("从数组中随机选择一个元素")
    String randomChoice(String[] choices)
}
```

### 5. UnitConverter（单位转换）

```java
public static class UnitConverter {
    @Tool("摄氏度转华氏度")
    double celsiusToFahrenheit(double celsius)

    @Tool("华氏度转摄氏度")
    double fahrenheitToCelsius(double fahrenheit)

    @Tool("千米转英里")
    double kilometersToMiles(double kilometers)

    @Tool("英里转千米")
    double milesToKilometers(double miles)

    @Tool("千克转磅")
    double kilogramsToPounds(double kilograms)

    @Tool("磅转千克")
    double poundsToKilograms(double pounds)
}
```

### 6. Validator（数据验证）

```java
public static class Validator {
    @Tool("验证邮箱地址格式")
    boolean isValidEmail(String email)

    @Tool("验证URL格式")
    boolean isValidUrl(String url)

    @Tool("验证中国手机号码")
    boolean isValidChinesePhone(String phone)

    @Tool("验证身份证号码")
    boolean isValidIdCard(String idCard)
}
```

## API 使用示例

### 请求格式

```json
{
  "message": "用户消息",
  "enabledTools": ["工具类型1", "工具类型2"]
}
```

### 工具类型

- `calculator` - 计算器
- `datetime` - 日期时间
- `text` - 文本处理
- `random` - 随机数生成
- `converter` - 单位转换
- `validator` - 数据验证

### 示例 1: 使用计算器

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 25 + 17 的结果",
    "enabledTools": ["calculator"]
  }'
```

**响应**:
```json
{
  "success": true,
  "content": "25 + 17 的结果是 42。",
  "toolExecutions": []
}
```

**注意**: 使用 `@Tool` 注解方式时，`toolExecutions` 字段为空，因为工具调用是自动的，无法获取详细的调用信息。

### 示例 2: 使用多个工具

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 25 + 17，然后检查 test@example.com 是否是有效邮箱",
    "enabledTools": ["calculator", "validator"]
  }'
```

### 示例 3: 使用所有工具（默认）

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 100 除以 4，然后把结果转换成英尺"
  }'
```

如果不指定 `enabledTools`，系统会使用所有 6 种工具类型。

### 示例 4: 复杂任务

```bash
curl -X POST http://localhost:8080/api/tool/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "先计算 100 减 25，然后把结果乘以 3，最后加上 50",
    "enabledTools": ["calculator"]
  }'
```

AI 会自动决定调用哪些具体的工具方法（subtract、multiply、add）来完成任务。

## 实现原理

### 代码结构

```java
@Service
public class ToolServiceImpl implements ToolService {

    @Override
    public ToolCallResponse chatWithTools(ToolCallRequestVo requestVo) {
        // 1. 根据 enabledTools 创建工具实例
        List<Object> tools = buildTools(requestVo.getEnabledTools());

        // 2. 使用 AiServices 构建助手，注入工具
        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
            .chatLanguageModel(chatModel)
            .tools(tools.toArray())  // 传入工具实例
            .build();

        // 3. 调用 AI（工具自动执行）
        String response = assistant.chat(requestVo.getMessage());

        return ToolCallResponse.builder()
            .success(true)
            .content(response)
            .build();
    }

    private List<Object> buildTools(List<String> enabledTools) {
        List<Object> tools = new ArrayList<>();
        for (String toolType : enabledTools) {
            tools.add(createToolInstance(toolType));
        }
        return tools;
    }

    private Object createToolInstance(String toolType) {
        return switch (toolType.toLowerCase()) {
            case "calculator" -> new BuiltInTools.Calculator();
            case "datetime" -> new BuiltInTools.DateTime();
            case "text" -> new BuiltInTools.TextProcessor();
            case "random" -> new BuiltInTools.RandomGenerator();
            case "converter" -> new BuiltInTools.UnitConverter();
            case "validator" -> new BuiltInTools.Validator();
            default -> null;
        };
    }
}
```

### 工作流程

```
用户请求
  ↓
1. 根据 enabledTools 创建工具实例
  ↓
2. AiServices.builder() 构建助手
  ↓
3. 传入工具实例 .tools(tools.toArray())
  ↓
4. assistant.chat(message)
  ↓
   ├─ AI 分析消息
   ├─ AI 决定调用哪些工具
   ├─ LangChain4j 自动执行工具
   └─ AI 生成最终回复
  ↓
返回结果
```

## 优势总结

### ✅ 使用 @Tool 注解的场景

1. **稳定的内置工具**：工具定义不会频繁变化
2. **快速开发**：减少样板代码，专注业务逻辑
3. **类型安全**：编译时检查，减少运行时错误
4. **简洁清晰**：工具定义和实现在一起

### ❌ 不适合 @Tool 注解的场景

1. **需要详细的工具调用信息**：无法获取工具调用的参数和结果
2. **动态工具配置**：需要在运行时动态修改工具定义
3. **复杂的工具编排**：需要手动控制工具调用顺序和逻辑

对于这些场景，可以使用 `ToolSpecification` 方式（参考之前的实现）。

## 测试用例

运行测试：

```bash
# 测试计算器工具
mvn test -Dtest=ToolServiceTest#testCalculatorTool

# 测试复杂计算
mvn test -Dtest=ToolServiceTest#testComplexCalculation

# 测试日期时间工具
mvn test -Dtest=ToolServiceTest#testDateTimeTool

# 测试文本处理工具
mvn test -Dtest=ToolServiceTest#testTextProcessorTool

# 测试数据验证工具
mvn test -Dtest=ToolServiceTest#testValidatorTool

# 测试单位转换工具
mvn test -Dtest=ToolServiceTest#testUnitConverterTool

# 测试多种工具组合
mvn test -Dtest=ToolServiceTest#testMultipleToolTypes

# 测试所有工具（默认）
mvn test -Dtest=ToolServiceTest#testAllTools
```

## 扩展新工具

### 步骤 1: 在 BuiltInTools 中添加新工具类

```java
public static class MyNewTool {
    @Tool("工具描述")
    public String myMethod(@P("参数描述") String param) {
        // 实现逻辑
        return result;
    }
}
```

### 步骤 2: 在 ToolServiceImpl 中添加工具实例创建

```java
private Object createToolInstance(String toolType) {
    return switch (toolType.toLowerCase()) {
        case "calculator" -> new BuiltInTools.Calculator();
        case "datetime" -> new BuiltInTools.DateTime();
        // ... 其他工具
        case "mynew" -> new BuiltInTools.MyNewTool();  // 添加这行
        default -> null;
    };
}
```

### 步骤 3: 在 buildDefaultTools 中添加（可选）

如果希望新工具默认启用：

```java
private List<Object> buildDefaultTools() {
    return List.of(
        new BuiltInTools.Calculator(),
        new BuiltInTools.DateTime(),
        // ... 其他工具
        new BuiltInTools.MyNewTool()  // 添加这行
    );
}
```

### 步骤 4: 更新 Controller 的可用工具列表

```java
@GetMapping("/available")
public AvailableToolsResponse getAvailableTools() {
    return new AvailableToolsResponse(List.of(
        // ... 其他工具
        new ToolInfo("mynew", "我的新工具", "工具分类")
    ));
}
```

## 常见问题

### Q1: 为什么 toolExecutions 返回空列表？

使用 `@Tool` 注解方式时，工具调用由 LangChain4j 自动处理，我们无法直接获取工具调用的详细信息。如果需要详细信息，可以使用 `ToolSpecification` 方式。

### Q2: 如何查看工具调用的详细过程？

启用日志：
```yaml
logging:
  level:
    dev.langchain4j: DEBUG
```

### Q3: AI 没有调用工具怎么办？

可能原因：
1. 工具描述不够清晰
2. 用户消息不明确
3. AI 认为不需要工具就能回答

建议在消息中明确要求使用工具：
```json
{
  "message": "请使用计算器工具计算 25 + 17"
}
```

### Q4: 如何同时使用 @Tool 和 ToolSpecification？

可以在同一个请求中混合使用：
```java
List<Object> tools = new ArrayList<>();
tools.add(new BuiltInTools.Calculator());  // @Tool 方式

ToolSpecification customSpec = ToolSpecification.builder()
    .name("custom")
    .description("自定义工具")
    .build();
// 需要单独处理 ToolSpecification
```

但建议选择一种方式保持一致性。

## 总结

现在 `/api/tool/chat` 接口直接使用 `BuiltInTools` 中定义的工具：

✅ **优点**:
- 代码简洁，易于维护
- 工具定义和实现在一起
- 自动执行，无需手动管理
- 类型安全

❌ **限制**:
- 无法获取详细的工具调用信息
- 不适合需要动态配置的场景

对于大多数场景，`@Tool` 注解方式已经足够使用。如果需要更细粒度的控制，可以考虑使用 `ToolSpecification` 方式。
