# LangChain4j ToolExecution API 说明

## 问题背景

在实现工具调用功能时，需要从 `Result<String>` 对象中提取工具执行的详细信息（工具名称、参数、结果），以便在前端展示。

## API 结构

### 1. Result 类

`dev.langchain4j.service.Result<T>` 包含以下关键方法：

- `content()` - 返回 AI 的文本响应
- `tokenUsage()` - 返回 token 消耗统计
- **`toolExecutions()`** - 返回 `List<ToolExecution>`，包含所有工具执行详情

### 2. ToolExecution 类

`dev.langchain4j.service.tool.ToolExecution` 包含以下方法：

- **`request()`** - 返回 `ToolExecutionRequest` 对象（工具调用请求信息）
- **`result()`** - 返回 `String`（工具执行结果）

### 3. ToolExecutionRequest 类

`dev.langchain4j.agent.tool.ToolExecutionRequest` 包含以下方法：

- **`name()`** - 返回 `String`（工具名称）
- **`arguments()`** - 返回 `String`（工具参数，JSON 格式）
- `id()` - 返回 `String`（工具调用请求 ID）

## 正确的使用方式

### 从 Result 提取工具执行信息

```java
Result<String> result = aiAssistantService.chat(userMessage);

// 获取工具执行列表
List<ToolExecution> toolExecutions = result.toolExecutions();

// 转换为前端需要的格式
List<ToolExecutionInfo> infos = toolExecutions.stream()
    .map(te -> ToolExecutionInfo.builder()
            .toolName(te.request().name())        // 工具名称
            .arguments(te.request().arguments())  // 工具参数
            .result(te.result())                  // 执行结果
            .build())
    .collect(Collectors.toList());
```

## 常见错误

### ❌ 错误做法 1：使用不存在的方法

```java
// 错误！ToolExecution 没有 toolName() 方法
te.toolName()

// 错误！ToolExecution 没有 arguments() 方法
te.arguments()
```

### ❌ 错误做法 2：使用错误的包

```java
// 错误！ToolExecution 在 service.tool 包中，不是 agent.tool
import dev.langchain4j.agent.tool.ToolExecution;  // ❌

// 正确！
import dev.langchain4j.service.tool.ToolExecution;  // ✅
```

### ✅ 正确做法：通过 request() 访问

```java
// 正确！通过 request() 获取 ToolExecutionRequest，再调用 name()
te.request().name()

// 正确！通过 request() 获取参数
te.request().arguments()

// 正确！直接调用 result() 获取结果
te.result()
```

## 完整示例

```java
@Service
@RequiredArgsConstructor
public class AIAssistantWithToolsServiceImpl implements AIAssistantWithToolsService {

    private final AIAssistantService aiAssistantService;

    @Override
    public AssistantResponse chatWithTools(String userMessage) {
        // AI 自动判断是否需要调用工具，并自动执行
        Result<String> result = aiAssistantService.chat(userMessage);

        // 返回完整信息
        return AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage())
                .toolExecutions(convertToolExecutions(result.toolExecutions()))
                .build();
    }

    /**
     * 转换 LangChain4j 的 ToolExecution 为前端需要的格式
     */
    private List<ToolExecutionInfo> convertToolExecutions(
            List<ToolExecution> toolExecutions) {

        if (toolExecutions == null || toolExecutions.isEmpty()) {
            return Collections.emptyList();
        }

        return toolExecutions.stream()
                .map(te -> ToolExecutionInfo.builder()
                        .toolName(te.request().name())        // ✅ 正确
                        .arguments(te.request().arguments())  // ✅ 正确
                        .result(te.result())                  // ✅ 正确
                        .build())
                .collect(Collectors.toList());
    }
}
```

## 数据流示例

假设用户输入："计算 25 + 17"

1. **AI 调用工具**：Calculator.add(25, 17)

2. **Result.toolExecutions() 返回**：
   ```java
   [
     ToolExecution {
       request: ToolExecutionRequest {
         name: "add",
         arguments: '{"arg0":25,"arg1":17}',
         id: "call_abc123"
       },
       result: "42.0"
     }
   ]
   ```

3. **转换后的前端格式**：
   ```java
   [
     ToolExecutionInfo {
       toolName: "add",
       arguments: '{"arg0":25,"arg1":17}',
       result: "42.0"
     }
   ]
   ```

4. **前端展示**：
   ```
   工具调用: add: 42.0
   ```

## 总结

- `Result.toolExecutions()` 返回 `List<ToolExecution>`
- `ToolExecution.request()` 返回 `ToolExecutionRequest`（包含工具名称和参数）
- `ToolExecution.result()` 返回工具执行结果
- 正确的访问路径：`te.request().name()` / `te.request().arguments()` / `te.result()`

这种设计将工具的**请求信息**（名称、参数）和**执行结果**分离，使得 API 结构更加清晰。
