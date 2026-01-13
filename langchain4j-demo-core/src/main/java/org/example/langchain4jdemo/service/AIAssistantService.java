package org.example.langchain4jdemo.service;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI 助手服务接口
 * 使用 AiServices.create() 手动创建实现类
 * 演示如何使用 LangChain4j 的声明式AI服务功能
 */
public interface AIAssistantService {

    /**
     * 简单聊天
     * @param userMessage 用户消息
     * @return AI 回复
     */
    @SystemMessage("You are a polite and helpful assistant.")
    Result<String> chat(String userMessage);

    /**
     * 带有自定义系统提示词的聊天
     * @param systemMessage 系统提示词
     * @param userMessage 用户消息
     * @return AI 回复（包含 TokenUsage）
     */
    @SystemMessage("{{systemMessage}}")
    @UserMessage("{{userMessage}}")
    Result<String> chatWithSystemMessage(@V("systemMessage") String systemMessage,
                                         @V("userMessage") String userMessage);

    /**
     * 带参数的聊天
     * @param language 语言
     * @param topic 话题
     * @return AI 回复（包含 TokenUsage）
     */
    @SystemMessage("You are a helpful assistant. Answer in {{language}}.")
    @UserMessage("Tell me about {{topic}}")
    Result<String> chatWithVariables(@V("language") String language,
                                     @V("topic") String topic);
}
