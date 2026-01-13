package org.example.langchain4jdemo.common;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ChatModelConfiguration {

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                // 如果要使用真实 OpenAI API，取消下面两行注释并设置你的 API Key
                // .baseUrl("https://api.openai.com/v1")
                // .apiKey("sk-your-api-key-here")
                //
                // 当前使用演示服务器（注意：演示服务器的流式响应有 charset=iso-8859-1 问题，导致中文显示为 ??）
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                // 如果要使用真实 OpenAI API，取消下面两行注释并设置你的 API Key
                // .baseUrl("https://api.openai.com/v1")
                // .apiKey("sk-your-api-key-here")
                //
                // 当前使用演示服务器（注意：演示服务器的流式响应有 charset=iso-8859-1 问题，导致中文显示为 ??）
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 创建 AIAssistantService 的实现
     * 使用 AiServices.create() 手动创建，演示声明式 AI 服务
     */
    @Bean
    public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
        return AiServices.create(AIAssistantService.class, chatModel);
    }
}
