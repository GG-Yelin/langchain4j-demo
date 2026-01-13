package org.example.langchain4jdemo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 简单聊天接口
     * POST /api/assistant/chat
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("AI Assistant chat request: {}", request.message);

        String response = aiAssistantService.chat(request.message);

        return new ChatResponse(response);
    }

    /**
     * 带自定义系统提示词的聊天
     * POST /api/assistant/chat-custom
     */
    @PostMapping("/chat-custom")
    public ChatResponse chatCustom(@RequestBody CustomChatRequest request) {
        log.info("AI Assistant custom chat request: system={}, message={}",
                request.systemMessage, request.message);

        String response = aiAssistantService.chatWithSystemMessage(
                request.systemMessage,
                request.message
        );

        return new ChatResponse(response);
    }

    /**
     * 带变量的聊天
     * POST /api/assistant/chat-variables
     */
    @PostMapping("/chat-variables")
    public ChatResponse chatWithVariables(@RequestBody VariableChatRequest request) {
        log.info("AI Assistant variable chat: language={}, topic={}",
                request.language, request.topic);

        String response = aiAssistantService.chatWithVariables(
                request.language,
                request.topic
        );

        return new ChatResponse(response);
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String response;
    }
}
