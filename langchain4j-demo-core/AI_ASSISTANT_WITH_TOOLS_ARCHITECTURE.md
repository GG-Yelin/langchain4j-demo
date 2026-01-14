# AI 助手工具调用架构说明

## 概述

本项目采用**详细版工具调用架构**，可以完整展示工具调用的过程和详情，包括工具名称、参数、执行结果和token消耗。

## 架构设计

### 双层实现

项目同时使用两种 AI 助手实现：

#### 1. AIAssistantService（简单模式 - 自动工具调用）

**用途**：基础对话功能，AI 自动调用工具但不返回详情

**配置**：
```java
// ChatModelConfiguration.java:71
@Bean
public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
    return AiServices.builder(AIAssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(
                new BuiltInTools.Calculator(),
                new BuiltInTools.DateTime(),
                new BuiltInTools.TextProcessor()
            )
            .build();
}
```

**特点**：
- ✅ LangChain4j 自动管理工具调用
- ✅ 代码简洁
- ❌ 无法获取工具调用详情
- ❌ 只返回最终文本结果

**使用场景**：
- `/api/assistant/chat` - 简单聊天（无工具详情）
- `/api/assistant/chat-custom` - 自定义提示词（无工具详情）
- `/api/assistant/chat-variables` - 变量模板（无工具详情）

#### 2. AIAssistantWithToolsService（详细模式 - 手动工具调用）

**用途**：完整展示工具调用过程和详情

**实现**：
```java
// AIAssistantWithToolsServiceImpl.java
@Service
@RequiredArgsConstructor
public class AIAssistantWithToolsServiceImpl implements AIAssistantWithToolsService {

    // 直接使用已配置工具的 AIAssistantService
    private final AIAssistantService aiAssistantService;

    public AssistantResponse chatWithTools(String message) {
        // AI 自动判断是否需要调用工具，并自动执行
        Result<String> result = aiAssistantService.chat(message);

        // 返回完整信息
        return AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage())
                .toolExecutions(convertToolExecutions(result.toolExecutions()))  // ✅ 工具详情
                .build();
    }

    // 转换 ToolExecution 为前端格式
    private List<ToolExecutionInfo> convertToolExecutions(List<ToolExecution> toolExecutions) {
        return toolExecutions.stream()
                .map(te -> ToolExecutionInfo.builder()
                        .toolName(te.request().name())
                        .arguments(te.request().arguments())
                        .result(te.result())
                        .build())
                .collect(Collectors.toList());
    }
}
```

**特点**：
- ✅ 完整的工具调用详情（名称、参数、结果）
- ✅ 精确的token统计
- ✅ 便于调试和监控
- ✅ 代码简洁（直接使用 Result.toolExecutions()）

**使用场景**：
- `/api/assistant/chat-with-tools` - 简单聊天（带工具详情）
- `/api/assistant/chat-with-tools-custom` - 自定义提示词（带工具详情）
- `/api/assistant/chat-with-tools-variables` - 变量模板（带工具详情）

## API 端点对比

| 端点 | 工具支持 | 工具详情 | Token统计 | 实现 |
|-----|---------|---------|----------|------|
| `/api/assistant/chat` | ✅ 自动 | ❌ 无 | ⚠️ 总计 | AIAssistantService |
| `/api/assistant/chat-custom` | ✅ 自动 | ❌ 无 | ⚠️ 总计 | AIAssistantService |
| `/api/assistant/chat-variables` | ✅ 自动 | ❌ 无 | ⚠️ 总计 | AIAssistantService |
| `/api/assistant/chat-with-tools` | ✅ 手动 | ✅ 完整 | ✅ 分步 | AIAssistantWithToolsService |
| `/api/assistant/chat-with-tools-custom` | ✅ 手动 | ✅ 完整 | ✅ 分步 | AIAssistantWithToolsService |
| `/api/assistant/chat-with-tools-variables` | ✅ 手动 | ✅ 完整 | ✅ 分步 | AIAssistantWithToolsService |

## 工具调用流程详解

### 详细版流程（AIAssistantWithToolsServiceImpl）

```
用户输入: "计算 25 + 17 的结果"
    ↓
第一次 AI 调用
    ├─ 输入: 用户消息 + 工具规范
    ├─ AI 分析: 需要使用 Calculator.add 工具
    └─ 输出: ToolExecutionRequest { name: "add", args: {arg0: 25, arg1: 17} }
    ↓
执行工具 (后端)
    ├─ 查找工具: Calculator.add
    ├─ 解析参数: 25, 17
    ├─ 调用方法: add(25, 17)
    └─ 返回结果: 42.0
    ↓
第二次 AI 调用
    ├─ 输入: 原始消息 + 工具结果
    ├─ AI 生成: 友好的文本回复
    └─ 输出: "25 + 17 的结果是 42"
    ↓
返回给前端
    ├─ response: "25 + 17 的结果是 42"
    ├─ toolExecutions: [
    │   {
    │     toolName: "add",
    │     arguments: '{"arg0":25,"arg1":17}',
    │     result: "42.0"
    │   }
    │ ]
    └─ tokenUsage: { input: 150, output: 20, total: 170 }
```

### 前端展示效果

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
用户
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
计算 25 + 17 的结果

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AI 助手
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
25 + 17 的结果是 42

┌────────────────────────────────┐
│ 工具调用:                        │
│                                │
│ add: 42.0                      │  ← 黄色卡片
│                                │
│ Token消耗: 输入 150 / 输出 20 / │  ← 蓝色卡片
│ 总计 170                        │
└────────────────────────────────┘
```

## 可用工具列表

当前配置了3个工具类，共计25个工具方法：

### 1. Calculator（计算器）- 7个方法
- `calculate(expression)` - 执行数学表达式
- `add(a, b)` - 加法
- `subtract(a, b)` - 减法
- `multiply(a, b)` - 乘法
- `divide(a, b)` - 除法
- `sqrt(number)` - 平方根
- `power(base, exponent)` - 幂运算

### 2. DateTime（日期时间）- 6个方法
- `getCurrentDateTime()` - 当前日期时间
- `getCurrentDate()` - 当前日期
- `getDayOfWeek(date)` - 获取星期几
- `daysBetween(startDate, endDate)` - 计算天数差
- `addDays(date, days)` - 日期加减
- `isLeapYear(year)` - 判断闰年

### 3. TextProcessor（文本处理）- 7个方法
- `countWords(text)` - 统计字数
- `countCharacters(text)` - 统计字符数
- `toUpperCase(text)` - 转大写
- `toLowerCase(text)` - 转小写
- `reverseText(text)` - 反转文本
- `contains(text, substring)` - 包含检查
- `replace(text, oldText, newText)` - 文本替换

## 为什么选择详细版？

虽然详细版代码更复杂，但提供了以下关键优势：

1. **教育价值** 📚
   - 清晰展示 AI 如何判断和使用工具
   - 学习者可以理解完整的工具调用流程

2. **透明度** 🔍
   - 用户能看到 AI 调用了哪些工具
   - 增强对 AI 行为的信任

3. **调试友好** 🐛
   - 开发时可以追踪工具执行情况
   - 便于定位工具调用问题

4. **完整统计** 📊
   - 精确统计两次 AI 调用的 token
   - 帮助优化成本和性能

5. **演示效果** 🎯
   - 适合作为 Demo 展示工具调用能力
   - 直观展示 AI Agent 的工作原理

## 如何测试

### 1. 启动服务

```bash
# 后端
cd langchain4j-demo-core
mvn spring-boot:run

# 前端
cd langchain4j-demo-frontend
npm run dev
```

### 2. 访问页面

打开 http://localhost:5173，选择"AI助手"模式

### 3. 测试工具调用

选择带"(工具)"标记的模式，尝试以下问题：

#### 计算器测试
- "计算 25 + 17 的结果"
- "9 的平方根是多少？"
- "2 的 10 次方等于多少？"

#### 日期时间测试
- "今天是星期几？"
- "2024 年是闰年吗？"
- "2024-01-01 到 2024-12-31 有多少天？"

#### 文本处理测试
- "将 hello world 转换为大写"
- "反转文本 LangChain4j"
- "统计 'The quick brown fox' 有多少个单词"

### 4. 查看结果

每个响应都会显示：
- 🤖 AI 的最终回复
- 🛠️ 使用的工具（黄色卡片）
- 📊 Token 消耗（蓝色卡片）

## 总结

当前架构采用**详细版工具调用**方式，虽然代码相对复杂，但提供了：
- ✅ 完整的工具调用可见性
- ✅ 精确的 token 统计
- ✅ 优秀的调试和监控能力
- ✅ 最佳的演示和教学效果

这使得项目非常适合作为 **LangChain4j 工具调用的学习和演示案例**！
