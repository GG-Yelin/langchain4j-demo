package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ToolCallRequestVo;
import org.example.langchain4jdemo.dto.ToolCallResponse;
import org.example.langchain4jdemo.service.ToolService;
import org.example.langchain4jdemo.tools.BuiltInTools;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final OpenAiChatModel chatModel;

    // 缓存工具实例，避免重复创建
    private final Map<String, Object> toolInstanceCache = new HashMap<>();

    @Override
    public ToolCallResponse chatWithTools(ToolCallRequestVo requestVo) {
        try {
            log.info("Tool chat request: message={}, tools={}",
                    requestVo.getMessage(), requestVo.getEnabledTools());

            // 1. 根据请求构建工具实例和规范
            List<Object> toolInstances = buildTools(requestVo.getEnabledTools());

            if (toolInstances.isEmpty()) {
                log.warn("No tools enabled, using default tools");
                toolInstances = buildDefaultTools();
            }

            // 2. 从工具实例提取 ToolSpecification
            List<ToolSpecification> toolSpecs = extractToolSpecifications(toolInstances);

            log.info("Extracted {} tool specifications from {} tool instances",
                    toolSpecs.size(), toolInstances.size());

            // 3. 第一次 AI 调用（让 AI 决定是否需要调用工具）
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(UserMessage.from(requestVo.getMessage()));

            ChatRequest request = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs)
                    .build();

            ChatResponse response = chatModel.chat(request);
            AiMessage aiMessage = response.aiMessage();
            log.info("First call model, response = {}", aiMessage);

            // 初始化 token 计数器
            int totalInputTokens = 0;
            int totalOutputTokens = 0;

            // 累计第一次调用的 token
            if (response.tokenUsage() != null) {
                totalInputTokens += response.tokenUsage().inputTokenCount();
                totalOutputTokens += response.tokenUsage().outputTokenCount();
                log.info("First call token usage: input={}, output={}",
                        response.tokenUsage().inputTokenCount(),
                        response.tokenUsage().outputTokenCount());
            }

            // 4. 检查 AI 是否要调用工具
            if (!aiMessage.hasToolExecutionRequests()) {
                log.info("No tool execution requested");
                return ToolCallResponse.builder()
                        .success(true)
                        .content(aiMessage.text())
                        .toolExecutions(List.of())
                        .tokenUsage(ToolCallResponse.TokenUsage.builder()
                                .inputTokens(totalInputTokens)
                                .outputTokens(totalOutputTokens)
                                .totalTokens(totalInputTokens + totalOutputTokens)
                                .build())
                        .build();
            }

            // 5. 执行工具调用
            List<ToolCallResponse.ToolExecution> toolExecutions = new ArrayList<>();
            messages.add(aiMessage);

            for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
                log.info("Executing tool: {} with args: {}",
                        toolRequest.name(), toolRequest.arguments());

                try {
                    // 执行工具
                    String toolResult = executeToolByName(
                            toolInstances,
                            toolRequest.name(),
                            toolRequest.arguments()
                    );

                    log.info("Tool execution result: {}", toolResult);

                    // 记录工具执行
                    toolExecutions.add(ToolCallResponse.ToolExecution.builder()
                            .toolName(toolRequest.name())
                            .arguments(toolRequest.arguments())
                            .result(toolResult)
                            .build());

                    // 将工具执行结果添加到消息列表
                    messages.add(ToolExecutionResultMessage.from(toolRequest, toolResult));

                } catch (Exception e) {
                    log.error("Tool execution failed: {}", toolRequest.name(), e);
                    String errorResult = "工具执行失败: " + e.getMessage();

                    toolExecutions.add(ToolCallResponse.ToolExecution.builder()
                            .toolName(toolRequest.name())
                            .arguments(toolRequest.arguments())
                            .result(errorResult)
                            .build());

                    messages.add(ToolExecutionResultMessage.from(toolRequest, errorResult));
                }
            }

            // 6. 第二次 AI 调用（让 AI 根据工具结果生成最终回复）
            ChatRequest finalRequest = ChatRequest.builder()
                    .messages(messages)
                    .toolSpecifications(toolSpecs)
                    .build();

            ChatResponse finalResponse = chatModel.chat(finalRequest);
            String finalContent = finalResponse.aiMessage().text();

            // 累计第二次调用的 token
            if (finalResponse.tokenUsage() != null) {
                totalInputTokens += finalResponse.tokenUsage().inputTokenCount();
                totalOutputTokens += finalResponse.tokenUsage().outputTokenCount();
                log.info("Second call token usage: input={}, output={}",
                        finalResponse.tokenUsage().inputTokenCount(),
                        finalResponse.tokenUsage().outputTokenCount());
            }

            log.info("Final response: {}", finalContent);
            log.info("Total token usage: input={}, output={}, total={}",
                    totalInputTokens, totalOutputTokens, totalInputTokens + totalOutputTokens);

            // 7. 返回最终结果
            ToolCallResponse result = ToolCallResponse.builder()
                    .success(true)
                    .content(finalContent)
                    .toolExecutions(toolExecutions)
                    .tokenUsage(ToolCallResponse.TokenUsage.builder()
                            .inputTokens(totalInputTokens)
                            .outputTokens(totalOutputTokens)
                            .totalTokens(totalInputTokens + totalOutputTokens)
                            .build())
                    .build();

            log.info("Returning response: success={}, content={}, toolExecutions={}",
                    result.isSuccess(), result.getContent(), result.getToolExecutions().size());

            return result;

        } catch (Exception e) {
            log.error("Tool call error", e);
            return ToolCallResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .content(null)
                    .toolExecutions(List.of())
                    .build();
        }
    }

    /**
     * 根据工具类型名称构建工具实例列表
     */
    private List<Object> buildTools(List<String> enabledTools) {
        List<Object> tools = new ArrayList<>();

        if (enabledTools == null || enabledTools.isEmpty()) {
            return tools;
        }

        for (String toolType : enabledTools) {
            Object tool = createToolInstance(toolType);
            if (tool != null) {
                tools.add(tool);
                log.debug("Added tool: {}", toolType);
            }
        }

        return tools;
    }

    /**
     * 构建默认工具列表（所有工具）
     */
    private List<Object> buildDefaultTools() {
        return List.of(
                new BuiltInTools.Calculator(),
                new BuiltInTools.DateTime(),
                new BuiltInTools.TextProcessor(),
                new BuiltInTools.RandomGenerator(),
                new BuiltInTools.UnitConverter(),
                new BuiltInTools.Validator()
        );
    }

    /**
     * 根据类型名称创建工具实例
     */
    private Object createToolInstance(String toolType) {
        // 使用缓存避免重复创建
        return toolInstanceCache.computeIfAbsent(toolType.toLowerCase(), key ->
                switch (key) {
                    case "calculator", "calc" -> new BuiltInTools.Calculator();
                    case "datetime", "time" -> new BuiltInTools.DateTime();
                    case "text", "textprocessor" -> new BuiltInTools.TextProcessor();
                    case "random", "randomgenerator" -> new BuiltInTools.RandomGenerator();
                    case "converter", "unitconverter" -> new BuiltInTools.UnitConverter();
                    case "validator", "validate" -> new BuiltInTools.Validator();
                    default -> {
                        log.warn("Unknown tool type: {}", toolType);
                        yield null;
                    }
                }
        );
    }

    /**
     * 从工具实例提取 ToolSpecification
     * 使用 LangChain4j 的内置工具规范提取器
     */
    private List<ToolSpecification> extractToolSpecifications(List<Object> toolInstances) {
        List<ToolSpecification> specs = new ArrayList<>();

        for (Object toolInstance : toolInstances) {
            // 使用 LangChain4j 的 ToolSpecifications 工具类来提取
            List<ToolSpecification> instanceSpecs =
                    dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationsFrom(toolInstance);
            specs.addAll(instanceSpecs);
            log.debug("Extracted {} tools from {}", instanceSpecs.size(), toolInstance.getClass().getSimpleName());
        }

        return specs;
    }

    /**
     * 根据工具名称执行工具方法
     * 使用反射调用 @Tool 注解的方法
     */
    private String executeToolByName(List<Object> toolInstances, String toolName, String argumentsJson)
            throws Exception {

        // 遍历所有工具实例，查找匹配的方法
        for (Object toolInstance : toolInstances) {
            Method[] methods = toolInstance.getClass().getMethods();

            for (Method method : methods) {
                // 检查方法是否有 @Tool 注解
                if (!method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                    continue;
                }

                // 检查方法名是否匹配（LangChain4j 使用方法名作为工具名）
                String methodToolName = method.getName();
                if (!methodToolName.equals(toolName)) {
                    continue;
                }

                // 找到匹配的方法，解析参数并调用
                log.debug("Found matching tool method: {}.{}", toolInstance.getClass().getSimpleName(), methodToolName);

                Object result = invokeToolMethod(toolInstance, method, argumentsJson);

                // 将结果转换为字符串
                return result == null ? "null" : result.toString();
            }
        }

        throw new IllegalArgumentException("Tool not found: " + toolName);
    }

    /**
     * 调用工具方法
     */
    private Object invokeToolMethod(Object toolInstance, Method method, String argumentsJson) throws Exception {
        // 解析 JSON 参数
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> argsMap = objectMapper.readValue(argumentsJson, Map.class);

        // 获取方法参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];

        // 按参数顺序填充
        for (int i = 0; i < parameterTypes.length; i++) {
            String argKey = "arg" + i; // LangChain4j 使用 arg0, arg1, ... 作为参数名
            Object argValue = argsMap.get(argKey);

            if (argValue == null) {
                // 如果没有找到 argN，尝试使用参数名
                argValue = argsMap.values().toArray()[i];
            }

            // 类型转换
            args[i] = convertArgument(argValue, parameterTypes[i]);
        }

        // 调用方法
        return method.invoke(toolInstance, args);
    }

    /**
     * 参数类型转换
     */
    private Object convertArgument(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // 如果类型已经匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        // 数字类型转换
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == int.class || targetType == Integer.class) {
                return num.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return num.longValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return num.doubleValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return num.floatValue();
            }
        }

        // 字符串转换
        if (targetType == String.class) {
            return value.toString();
        }

        // 布尔值转换
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        }

        // 数组转换
        if (targetType.isArray() && value instanceof List) {
            List<?> list = (List<?>) value;
            return list.toArray((Object[]) java.lang.reflect.Array.newInstance(
                    targetType.getComponentType(), list.size()));
        }

        // 默认返回原值
        return value;
    }
}
