package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.service.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.controller.AIAssistantController;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.example.langchain4jdemo.service.AIAssistantWithToolsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 带工具调用支持的 AI 助手服务实现
 *
 * 直接使用已配置工具的 AIAssistantService，无需手动管理工具
 * LangChain4j 会自动处理工具调用的全过程
 *
 * 注意：由于 Result<String> 不包含工具调用详情，
 * 所以 toolExecutions 返回空列表，但工具仍会被自动调用
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

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(List.of())  // 无法获取工具调用详情，但工具会被自动调用
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

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(List.of())
                .build();
    }

    @Override
    public AIAssistantController.AssistantResponse chatWithToolsAndVariables(
            String language,
            String topic) {

        log.info("Chat with tools (variables): language={}, topic={}", language, topic);

        // 使用变量模板
        Result<String> result = aiAssistantService.chatWithVariables(language, topic);

        return AIAssistantController.AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                        ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                        : null)
                .toolExecutions(List.of())
                .build();
    }
}

