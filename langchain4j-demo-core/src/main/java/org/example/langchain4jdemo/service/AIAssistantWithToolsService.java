package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.controller.AIAssistantController;

/**
 * 带工具调用支持的 AI 助手服务
 * 提供详细的工具调用信息
 */
public interface AIAssistantWithToolsService {

    /**
     * 简单聊天（带工具支持）
     * @param userMessage 用户消息
     * @return AI 回复及工具调用详情
     */
    AIAssistantController.AssistantResponse chatWithTools(String userMessage);

    /**
     * 带自定义系统提示词的聊天（带工具支持）
     * @param systemMessage 系统提示词
     * @param userMessage 用户消息
     * @return AI 回复及工具调用详情
     */
    AIAssistantController.AssistantResponse chatWithToolsAndCustomSystem(
            String systemMessage,
            String userMessage
    );

    /**
     * 带变量的聊天（带工具支持）
     * @param language 语言
     * @param topic 话题
     * @return AI 回复及工具调用详情
     */
    AIAssistantController.AssistantResponse chatWithToolsAndVariables(
            String language,
            String topic
    );
}
