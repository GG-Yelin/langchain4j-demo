package org.example.langchain4jdemo.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.tools.BuiltInTools;
import org.springframework.stereotype.Service;

/**
 * 内置工具服务
 * 演示如何使用 @Tool 注解定义的工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuiltInToolService {

    private final OpenAiChatModel chatModel;

    /**
     * 使用所有内置工具进行聊天
     */
    public String chatWithAllTools(String userMessage) {
        log.info("Chat with all built-in tools: {}", userMessage);

        // 创建所有工具实例
        BuiltInTools.Calculator calculator = new BuiltInTools.Calculator();
        BuiltInTools.DateTime dateTime = new BuiltInTools.DateTime();
        BuiltInTools.TextProcessor textProcessor = new BuiltInTools.TextProcessor();
        BuiltInTools.RandomGenerator randomGenerator = new BuiltInTools.RandomGenerator();
        BuiltInTools.UnitConverter unitConverter = new BuiltInTools.UnitConverter();
        BuiltInTools.Validator validator = new BuiltInTools.Validator();

        // 构建 AI 助手，注入所有工具
        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(calculator, dateTime, textProcessor, randomGenerator, unitConverter, validator)
                .build();

        String response = assistant.chat(userMessage);
        log.info("Response: {}", response);

        return response;
    }

    /**
     * 只使用计算器工具
     */
    public String chatWithCalculator(String userMessage) {
        log.info("Chat with calculator: {}", userMessage);

        BuiltInTools.Calculator calculator = new BuiltInTools.Calculator();

        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(calculator)
                .build();

        return assistant.chat(userMessage);
    }

    /**
     * 只使用日期时间工具
     */
    public String chatWithDateTime(String userMessage) {
        log.info("Chat with date/time tools: {}", userMessage);

        BuiltInTools.DateTime dateTime = new BuiltInTools.DateTime();

        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(dateTime)
                .build();

        return assistant.chat(userMessage);
    }

    /**
     * 只使用文本处理工具
     */
    public String chatWithTextProcessor(String userMessage) {
        log.info("Chat with text processor: {}", userMessage);

        BuiltInTools.TextProcessor textProcessor = new BuiltInTools.TextProcessor();

        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(textProcessor)
                .build();

        return assistant.chat(userMessage);
    }

    /**
     * 自定义工具组合
     */
    public String chatWithCustomTools(String userMessage, String[] toolTypes) {
        log.info("Chat with custom tools: {} - {}", userMessage, String.join(", ", toolTypes));

        // 根据指定的工具类型创建实例
        Object[] tools = new Object[toolTypes.length];
        for (int i = 0; i < toolTypes.length; i++) {
            tools[i] = createToolInstance(toolTypes[i]);
        }

        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(tools)
                .build();

        return assistant.chat(userMessage);
    }

    /**
     * 根据类型名称创建工具实例
     */
    private Object createToolInstance(String toolType) {
        return switch (toolType.toLowerCase()) {
            case "calculator" -> new BuiltInTools.Calculator();
            case "datetime" -> new BuiltInTools.DateTime();
            case "text" -> new BuiltInTools.TextProcessor();
            case "random" -> new BuiltInTools.RandomGenerator();
            case "converter" -> new BuiltInTools.UnitConverter();
            case "validator" -> new BuiltInTools.Validator();
            default -> throw new IllegalArgumentException("Unknown tool type: " + toolType);
        };
    }

    /**
     * AI 助手接口
     */
    interface ToolAssistant {
        String chat(String message);
    }
}
