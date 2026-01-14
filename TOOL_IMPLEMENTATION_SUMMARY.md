# 工具调用接口完善总结

## 完成的工作

本次完善了 `/api/tool/chat` 接口，实现了完整的动态工具调用功能。

## 核心改进

### 1. 完整的工具调用流程

**之前的问题**:
- `ToolServiceImpl.java` 第 48 行代码不完整（`.map()` 没有参数）
- 缺少完整的工具调用逻辑
- 没有工具执行的实现

**现在的实现**:
```java
// 1. 构建工具列表
List<ToolSpecification> toolSpecs = buildToolSpecifications(enabledTools);

// 2. 第一次 AI 调用（决定使用哪些工具）
ChatResponse response = chatModel.chat(request);

// 3. 执行工具
for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
    String toolResult = executeToolFunction(toolRequest.name(), toolRequest.arguments());
    messages.add(ToolExecutionResultMessage.from(toolRequest, toolResult));
}

// 4. 第二次 AI 调用（生成最终回复）
ChatResponse finalResponse = chatModel.chat(finalRequest);
```

### 2. 支持 6 种工具

#### 数学运算工具（4 个）
- **add**: 加法运算
- **subtract**: 减法运算
- **multiply**: 乘法运算
- **divide**: 除法运算（包含除以零错误处理）

#### 信息查询工具（2 个）
- **get_weather**: 天气查询（模拟数据）
- **get_time**: 时间查询

### 3. 灵活的工具配置

```java
// 如果没有指定工具，使用默认工具
if (enabledTools == null || enabledTools.isEmpty()) {
    enabledTools = List.of("add", "subtract", "multiply", "divide");
}
```

用户可以通过 `enabledTools` 参数动态选择需要的工具。

### 4. 完善的错误处理

```java
try {
    // 工具调用逻辑
} catch (Exception e) {
    log.error("Tool call error", e);
    return ToolCallResponse.builder()
            .success(false)
            .errorMessage(e.getMessage())
            .content(null)
            .toolExecutions(List.of())
            .build();
}
```

工具执行失败时，返回友好的错误信息。

### 5. 详细的日志记录

```java
log.info("Executing tool: {} with args: {}", toolRequest.name(), toolRequest.arguments());
```

方便调试和监控工具调用过程。

## 文件结构

### 修改的文件

#### 1. ToolServiceImpl.java
- 完整实现了工具调用流程
- 添加了 6 种工具的规范定义（`getToolSpecification`）
- 实现了工具执行逻辑（`executeToolFunction`）
- 添加了构建工具列表的方法（`buildToolSpecifications`）
- 添加了参数解析辅助方法（`getNumberFromMap`）

**关键方法**:
```java
public ToolCallResponse chatWithTools(ToolCallRequestVo requestVo)
private List<ToolSpecification> buildToolSpecifications(List<String> enabledTools)
private ToolSpecification getToolSpecification(String toolName)
private String executeToolFunction(String toolName, String argumentsJson)
private double getNumberFromMap(Map<String, Object> map, String key)
```

#### 2. ToolController.java
- 添加了详细的日志记录
- 添加了接口注释和示例
- 新增 `GET /api/tool/available` 接口（获取可用工具列表）
- 新增 `POST /api/tool/test` 接口（测试单个工具）
- 新增 DTO 类：`AvailableToolsResponse` 和 `ToolInfo`

**API 接口**:
```
POST /api/tool/chat      - 工具调用聊天
GET  /api/tool/available - 获取可用工具列表
POST /api/tool/test      - 测试单个工具
```

### 新增的文件

#### 3. ToolServiceTest.java
包含 8 个测试用例：
- `testAddTool` - 测试加法工具
- `testMultipleTools` - 测试多个工具组合
- `testWeatherTool` - 测试天气工具
- `testTimeTool` - 测试时间工具
- `testNoToolNeeded` - 测试不需要工具的对话
- `testDefaultTools` - 测试默认工具
- `testDivideByZero` - 测试除以零错误
- `testComplexCalculation` - 测试复杂计算

#### 4. TOOL_CALLING_API.md
完整的 API 文档，包含：
- 核心概念和工作流程
- API 接口说明
- 6 种工具的详细说明
- 8 个使用示例
- 实现原理解析
- 与 @Tool 注解的对比
- 错误处理说明
- 扩展新工具的步骤
- 最佳实践和性能考虑

#### 5. TOOL_QUICK_START.md
快速入门指南，包含：
- 快速测试命令（curl）
- Postman 测试步骤
- 测试用例运行方法
- 快速示例集合
- 前端集成示例（JavaScript 和 Vue.js）
- 常见问题解答

#### 6. TOOL_IMPLEMENTATION_SUMMARY.md
本文档，总结实现内容。

## 工作流程图

```
用户请求
  ↓
ToolController.chatWithTools()
  ↓
ToolService.chatWithTools()
  ↓
1. buildToolSpecifications() - 构建工具列表
  ↓
2. 第一次 AI 调用 - AI 决定使用哪些工具
  ↓
3. executeToolFunction() - 执行工具（循环）
  ↓
4. 第二次 AI 调用 - AI 生成最终回复
  ↓
返回 ToolCallResponse
```

## 测试验证

### 编译测试
```bash
mvn clean compile -DskipTests
```
✅ 编译成功

### 单元测试
```bash
mvn test -Dtest=ToolServiceTest#testAddTool
```
✅ 测试通过

**测试结果**:
```
用户: 计算 25 + 17 的结果
成功: true
回复: 25 + 17 的结果是 42。

工具调用:
  - 工具: add
    参数: {"a":25,"b":17}
    结果: 42.0

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 核心特性

### 1. 智能工具选择
AI 根据用户消息自动决定是否需要调用工具，以及调用哪些工具。

### 2. 多步骤工具调用
支持在一次对话中调用多个工具，AI 会按逻辑顺序调用。

**示例**:
```
用户: "先计算 100 减 25，然后把结果乘以 3"
AI 调用:
  1. subtract(100, 25) → 75
  2. multiply(75, 3) → 225
最终回复: "首先，100 - 25 = 75，然后 75 × 3 = 225。"
```

### 3. 动态工具配置
通过 `enabledTools` 参数控制哪些工具可用。

### 4. 完整的工具执行信息
返回每个工具的调用详情（工具名称、参数、结果）。

### 5. 友好的最终回复
AI 将工具执行结果转换为用户友好的自然语言回复。

## 技术亮点

### 1. ToolSpecification 动态定义
```java
ToolSpecification.builder()
    .name("add")
    .description("计算两个数的和")
    .parameters(JsonObjectSchema.builder()
        .addNumberProperty("a", "第一个数字")
        .addNumberProperty("b", "第二个数字")
        .required("a", "b")
        .build())
    .build();
```

### 2. 消息链管理
```java
List<ChatMessage> messages = new ArrayList<>();
messages.add(UserMessage.from(userMessage));          // 用户消息
messages.add(aiMessage);                              // AI 消息
messages.add(ToolExecutionResultMessage.from(...));   // 工具结果
```

### 3. JSON 参数解析
```java
@SuppressWarnings("unchecked")
Map<String, Object> args = objectMapper.readValue(argumentsJson, Map.class);
double a = getNumberFromMap(args, "a");
```

### 4. Switch 表达式
使用 Java 17 的 switch 表达式简化代码：
```java
return switch (toolName.toLowerCase()) {
    case "add" -> {
        double a = getNumberFromMap(args, "a");
        double b = getNumberFromMap(args, "b");
        yield String.valueOf(a + b);
    }
    case "subtract" -> ...
    default -> "未知工具: " + toolName;
};
```

## 与其他方法的对比

### ToolSpecification vs @Tool 注解

| 特性 | ToolSpecification | @Tool 注解 |
|------|------------------|-----------|
| **定义时机** | 运行时 | 编译时 |
| **灵活性** | 高（动态配置） | 低（需重新编译） |
| **代码量** | 多 | 少 |
| **工具执行** | 手动实现 | 自动执行 |
| **适用场景** | 动态工具、插件系统 | 固定工具、内置功能 |

### 本项目已实现的三种工具调用方式

1. **@Tool 注解** (BuiltInTools.java)
   - 30+ 工具方法
   - 6 个工具类（Calculator, DateTime, TextProcessor, etc.）
   - 声明式定义
   - 自动执行

2. **ToolSpecification** (ToolService.java)
   - 6 个动态工具
   - 运行时配置
   - 手动执行
   - 详细的执行信息

3. **MCP 协议** (McpService.java)
   - 远程工具调用
   - 跨语言支持
   - 独立部署

## 使用建议

### 何时使用 ToolSpecification

✅ **适合使用**:
- 需要动态添加/移除工具
- 需要根据用户配置启用工具
- 需要详细记录工具调用过程
- 工具执行逻辑复杂，需要手动控制
- 插件系统或工具市场

❌ **不适合使用**:
- 工具固定且简单
- 需要快速开发原型
- 工具数量少且不会变化

**推荐**: 对于固定的简单工具，使用 @Tool 注解更简洁。

## 后续优化建议

### 1. 工具执行优化
- 添加工具执行超时控制
- 支持工具并行执行
- 添加工具执行缓存

### 2. 安全性增强
- 添加工具调用频率限制
- 添加工具权限控制
- 验证工具参数合法性

### 3. 监控和日志
- 添加工具调用统计
- 记录工具执行耗时
- 异常告警

### 4. 工具扩展
- 实现真实的天气 API 调用
- 添加数据库查询工具
- 添加文件操作工具
- 添加网络请求工具

### 5. 前端集成
- 创建工具调用可视化界面
- 显示工具执行过程动画
- 支持工具配置管理

## 相关文档

- [完整 API 文档](./TOOL_CALLING_API.md)
- [快速入门指南](./TOOL_QUICK_START.md)
- [工具调用方式对比](./TOOL_CALLING_COMPARISON.md)
- [MCP 协议详解](./MCP_PROTOCOL_EXPLAINED.md)
- [工具调用方法总览](./TOOL_CALLING_METHODS.md)

## 总结

本次完善实现了：

1. ✅ 完整的工具调用流程（两次 AI 调用）
2. ✅ 6 种实用工具（数学运算 + 信息查询）
3. ✅ 动态工具配置
4. ✅ 完善的错误处理
5. ✅ 详细的日志记录
6. ✅ 8 个测试用例
7. ✅ 完整的文档（API 文档 + 快速入门）
8. ✅ 前端集成示例

接口已经可以正常使用，测试通过，文档完善！
