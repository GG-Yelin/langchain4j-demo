package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.controller.AIAssistantController;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.example.langchain4jdemo.service.AIAssistantWithToolsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 带工具调用支持的 AI 助手服务实现
 *
 * 直接使用已配置工具的 AIAssistantService，无需手动管理工具
 * LangChain4j 会自动处理工具调用的全过程，并通过 Result.toolExecutions() 返回详情
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantWithToolsServiceImpl implements AIAssistantWithToolsService {

    // 这个 AIAssistantService 已经在 ChatModelConfiguration 中配置了工具
    // 包括：Calculator, DateTime, TextProcessor
    private final AIAssistantService aiAssistantService;

    @Override
    public AIAssistantController.AssistantResponse chatWithTools(String userMessage) {
        log.info("Chat with tools: {}", userMessage);

        // AI 自动判断是否需要调用工具，并自动执行
        Result<String> result = aiAssistantService.chat(userMessage);

        log.info("Response: {}", result.content());
        log.info("Tool executions: {}", result.toolExecutions());

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(convertToolExecutions(result.toolExecutions()))
                .build();
    }

    @Override
    public AIAssistantController.AssistantResponse chatWithToolsAndCustomSystem(
            String systemMessage,
            String userMessage) {

        log.info("Chat with tools (custom system): system={}, message={}",
                systemMessage, userMessage);

        // 使用自定义系统提示词
        Result<String> result = aiAssistantService.chatWithSystemMessage(
                systemMessage,
                userMessage
        );

        log.info("Tool executions: {}", result.toolExecutions());

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(convertToolExecutions(result.toolExecutions()))
                .build();
    }

    @Override
    public AIAssistantController.AssistantResponse chatWithToolsAndVariables(
            String language,
            String topic) {

        log.info("Chat with tools (variables): language={}, topic={}", language, topic);

        // 使用变量模板
        Result<String> result = aiAssistantService.chatWithVariables(language, topic);

        log.info("Tool executions: {}", result.toolExecutions());

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(convertToolExecutions(result.toolExecutions()))
                .build();
    }

    /**
     * 转换 LangChain4j 的 ToolExecution 为前端需要的格式
     */
    private List<AIAssistantController.ToolExecutionInfo> convertToolExecutions(
            List<ToolExecution> toolExecutions) {

        if (toolExecutions == null || toolExecutions.isEmpty()) {
            return Collections.emptyList();
        }

        return toolExecutions.stream()
                .map(te -> AIAssistantController.ToolExecutionInfo.builder()
                        .toolName(te.request().name())
                        .arguments(te.request().arguments())
                        .result(te.result())
                        .build())
                .collect(Collectors.toList());
    }
}

