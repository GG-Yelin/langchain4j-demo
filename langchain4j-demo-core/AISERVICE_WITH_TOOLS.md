# 如何在 AiService 中指定 @Tool

## 概述

LangChain4j 提供了多种方式在创建 `AiService` 时指定工具（@Tool）。本文档详细说明各种方法的使用。

## 方法对比

### 方法1：AiServices.create() - 简单方式（无工具支持）

最简单的创建方式，但**不支持工具调用**：

```java
@Bean
public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
    return AiServices.create(AIAssistantService.class, chatModel);
}
```

**适用场景**：
- 只需要基础对话功能
- 不需要函数调用/工具使用

### 方法2：AiServices.builder().tools() - 指定单个工具

使用 builder 模式，逐个添加工具：

```java
@Bean
public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
    return AiServices.builder(AIAssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(new BuiltInTools.Calculator())
            .tools(new BuiltInTools.DateTime())
            .tools(new BuiltInTools.TextProcessor())
            .build();
}
```

**优点**：
- 清晰明了，每个工具独立一行
- 便于注释掉某些工具进行测试

### 方法3：AiServices.builder().tools() - 指定多个工具（推荐）

一次性传入多个工具实例：

```java
@Bean
public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
    return AiServices.builder(AIAssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(
                new BuiltInTools.Calculator(),
                new BuiltInTools.DateTime(),
                new BuiltInTools.TextProcessor(),
                new BuiltInTools.RandomGenerator(),
                new BuiltInTools.UnitConverter(),
                new BuiltInTools.Validator()
            )
            .build();
}
```

**优点**：
- 代码紧凑
- 适合工具数量较多的场景

### 方法4：使用 List 动态配置工具

使用列表动态管理工具：

```java
@Bean
public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
    // 可以根据配置动态选择工具
    List<Object> tools = new ArrayList<>();
    tools.add(new BuiltInTools.Calculator());
    tools.add(new BuiltInTools.DateTime());

    // 根据条件添加更多工具
    if (enableTextTools) {
        tools.add(new BuiltInTools.TextProcessor());
    }

    return AiServices.builder(AIAssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(tools)
            .build();
}
```

**适用场景**：
- 需要根据配置动态选择工具
- 工具需要条件性加载

### 方法5：注入 Spring Bean 工具

如果工具是 Spring 管理的 Bean（例如需要依赖注入）：

```java
// 首先定义工具为 Bean
@Configuration
public class ToolsConfiguration {

    @Bean
    public BuiltInTools.Calculator calculator() {
        return new BuiltInTools.Calculator();
    }

    @Bean
    public BuiltInTools.DateTime dateTime() {
        return new BuiltInTools.DateTime();
    }
}

// 然后注入使用
@Bean
public AIAssistantService aiAssistantService(
        OpenAiChatModel chatModel,
        BuiltInTools.Calculator calculator,
        BuiltInTools.DateTime dateTime) {

    return AiServices.builder(AIAssistantService.class)
            .chatLanguageModel(chatModel)
            .tools(calculator, dateTime)
            .build();
}
```

**适用场景**：
- 工具需要依赖注入（如数据库访问、HTTP客户端等）
- 工具需要被多个 AiService 共享

## 完整示例：创建带工具的 AiService

以下是一个完整的配置示例：

```java
package org.example.langchain4jdemo.common;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.example.langchain4jdemo.tools.BuiltInTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServiceWithToolsConfiguration {

    /**
     * 创建带所有工具的 AI 助手
     */
    @Bean
    public AIAssistantService aiAssistantServiceWithAllTools(OpenAiChatModel chatModel) {
        return AiServices.builder(AIAssistantService.class)
                .chatLanguageModel(chatModel)
                .tools(
                    new BuiltInTools.Calculator(),      // 数学计算
                    new BuiltInTools.DateTime(),        // 日期时间
                    new BuiltInTools.TextProcessor(),   // 文本处理
                    new BuiltInTools.RandomGenerator(), // 随机数生成
                    new BuiltInTools.UnitConverter(),   // 单位转换
                    new BuiltInTools.Validator()        // 数据验证
                )
                .build();
    }

    /**
     * 创建只带计算工具的 AI 助手
     */
    @Bean("calculatorAssistant")
    public AIAssistantService calculatorAssistant(OpenAiChatModel chatModel) {
        return AiServices.builder(AIAssistantService.class)
                .chatLanguageModel(chatModel)
                .tools(new BuiltInTools.Calculator())
                .build();
    }

    /**
     * 创建只带日期时间工具的 AI 助手
     */
    @Bean("dateTimeAssistant")
    public AIAssistantService dateTimeAssistant(OpenAiChatModel chatModel) {
        return AiServices.builder(AIAssistantService.class)
                .chatLanguageModel(chatModel)
                .tools(new BuiltInTools.DateTime())
                .build();
    }
}
```

## AiServices.builder() 其他常用配置

除了 `.tools()` 之外，还有其他有用的配置：

```java
return AiServices.builder(AIAssistantService.class)
        .chatLanguageModel(chatModel)                    // 必需：指定语言模型
        .tools(tools)                                     // 可选：指定工具
        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))  // 可选：配置记忆
        .contentRetriever(contentRetriever)              // 可选：配置RAG检索器
        .moderationModel(moderationModel)                // 可选：内容审核模型
        .logRequests(true)                               // 可选：记录请求日志
        .logResponses(true)                              // 可选：记录响应日志
        .build();
```

## 实际使用示例

创建 AiService 后，AI 会自动选择合适的工具来完成任务：

```java
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AIAssistantService aiAssistantService;

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        // AI 会自动判断是否需要调用工具
        Result<String> result = aiAssistantService.chat(message);
        return result.content();
    }
}
```

**示例对话**：

```
用户: "计算 25 + 17 的结果"
AI: 自动调用 Calculator.add(25, 17)
响应: "25 + 17 的结果是 42"

用户: "今天是星期几？"
AI: 自动调用 DateTime.getCurrentDate() 和 DateTime.getDayOfWeek()
响应: "今天是2026年1月14日，星期三"

用户: "你好"
AI: 不需要工具，直接回复
响应: "你好！我是你的AI助手，有什么可以帮助你的吗？"
```

## 工具调用的工作原理

1. **第一次调用**：AI 分析用户请求，决定是否需要调用工具
2. **工具执行**：如果需要，LangChain4j 自动执行对应的 @Tool 方法
3. **第二次调用**：AI 基于工具执行结果生成最终回复

这个过程对开发者完全透明，只需要配置好工具即可！

## 常见问题

### Q: 如何让 AI 只使用特定工具？
A: 创建 AiService 时只指定你想要的工具即可。

### Q: 工具调用会消耗更多 token 吗？
A: 是的，每次工具调用涉及两次 AI 请求（决策 + 生成回复），token 消耗会增加。

### Q: 如何查看工具调用的日志？
A: 在配置中启用 `.logRequests(true)` 和 `.logResponses(true)`，或者在 application.yml 中配置日志级别。

### Q: 工具方法抛出异常怎么办？
A: AI 会收到错误信息，并尝试给用户一个友好的错误提示。

### Q: 可以在运行时动态更改工具吗？
A: 不能。工具在创建 AiService 时就固定了。如果需要动态工具，可以创建多个不同的 AiService Bean。

## 最佳实践

1. **按功能分组**：为不同场景创建不同的 AiService（如 calculatorAssistant、textAssistant）
2. **最小化工具集**：只添加必要的工具，减少 token 消耗和 AI 决策时间
3. **清晰的工具描述**：@Tool 注解的描述要清晰，帮助 AI 正确选择工具
4. **参数说明**：使用 @P 注解清楚说明每个参数的用途和格式
5. **错误处理**：在工具方法中做好异常处理，返回友好的错误信息

## 参考资料

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [AiServices API 文档](https://docs.langchain4j.dev/tutorials/ai-services)
- [Tool 注解文档](https://docs.langchain4j.dev/tutorials/tools)
