package org.example.langchain4jdemo.controller;

import dev.langchain4j.service.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.springframework.web.bind.annotation.*;


/**
 * AI 助手控制器
 * 演示如何使用 AiServices 创建声明式 AI 服务
 */
@Slf4j
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AIAssistantController {

    private final AIAssistantService aiAssistantService;
    private final org.example.langchain4jdemo.service.AIAssistantWithToolsService aiAssistantWithToolsService;

    /**
     * 简单聊天接口
     * POST /api/assistant/chat
     */
    @PostMapping("/chat")
    public AssistantResponse chat(@RequestBody ChatRequest request) {
        log.info("AI Assistant chat request: {}", request.message);

        Result<String> result = aiAssistantService.chat(request.message);

        return AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                    ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                    : null)
                .build();
    }

    /**
     * 带自定义系统提示词的聊天
     * POST /api/assistant/chat-custom
     */
    @PostMapping("/chat-custom")
    public AssistantResponse chatCustom(@RequestBody CustomChatRequest request) {
        log.info("AI Assistant custom chat request: system={}, message={}",
                request.systemMessage, request.message);

        Result<String> result = aiAssistantService.chatWithSystemMessage(
                request.systemMessage,
                request.message
        );

        return AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                    ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                    : null)
                .build();
    }

    /**
     * 带变量的聊天
     * POST /api/assistant/chat-variables
     */
    @PostMapping("/chat-variables")
    public AssistantResponse chatWithVariables(@RequestBody VariableChatRequest request) {
        log.info("AI Assistant variable chat: language={}, topic={}",
                request.language, request.topic);

        Result<String> result = aiAssistantService.chatWithVariables(
                request.language,
                request.topic
        );

        return AssistantResponse.builder()
                .response(result.content())
                .tokenUsage(result.tokenUsage() != null
                    ? ChatResponseVO.TokenUsageVO.from(result.tokenUsage())
                    : null)
                .build();
    }

    /**
     * 带工具调用的简单聊天
     * POST /api/assistant/chat-with-tools
     */
    @PostMapping("/chat-with-tools")
    public AssistantResponse chatWithTools(@RequestBody ChatRequest request) {
        log.info("AI Assistant chat with tools request: {}", request.message);
        return aiAssistantWithToolsService.chatWithTools(request.message);
    }

    /**
     * 带工具调用和自定义系统提示词的聊天
     * POST /api/assistant/chat-with-tools-custom
     */
    @PostMapping("/chat-with-tools-custom")
    public AssistantResponse chatWithToolsCustom(@RequestBody CustomChatRequest request) {
        log.info("AI Assistant chat with tools (custom): system={}, message={}",
                request.systemMessage, request.message);
        return aiAssistantWithToolsService.chatWithToolsAndCustomSystem(
                request.systemMessage,
                request.message
        );
    }

    /**
     * 带工具调用和变量的聊天
     * POST /api/assistant/chat-with-tools-variables
     */
    @PostMapping("/chat-with-tools-variables")
    public AssistantResponse chatWithToolsVariables(@RequestBody VariableChatRequest request) {
        log.info("AI Assistant chat with tools (variables): language={}, topic={}",
                request.language, request.topic);
        return aiAssistantWithToolsService.chatWithToolsAndVariables(
                request.language,
                request.topic
        );
    }

    // 请求和响应对象
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomChatRequest {
        private String systemMessage;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariableChatRequest {
        private String language;
        private String topic;
    }

    /**
     * AI 助手响应对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssistantResponse {
        /**
         * AI 回复内容
         */
        private String response;

        /**
         * Token 使用情况
         */
        private ChatResponseVO.TokenUsageVO tokenUsage;

        /**
         * 工具调用信息（如果有）
         */
        private java.util.List<ToolExecutionInfo> toolExecutions;
    }

    /**
     * 工具执行信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolExecutionInfo {
        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具参数
         */
        private String arguments;

        /**
         * 执行结果
         */
        private String result;
    }
}
