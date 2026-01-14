package org.example.langchain4jdemo.common;

import dev.langchain4j.data.message.ToolExecutionResultMessage;
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
     *
     * 注意：这个bean不包含工具，如果需要工具支持，请使用 aiAssistantServiceWithTools
     */
//    @Bean
//    public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
//        return AiServices.create(AIAssistantService.class, chatModel);
//    }

    /**
     * 创建带工具的 AIAssistantService
     * 演示如何在 AiService 中指定 @Tool
     *
     * 注意：这个 AIAssistantService 配置了工具，但只能返回最终文本结果
     * 如果需要工具调用的详细信息（工具名、参数、结果），
     * 应使用 AIAssistantWithToolsService（详细版）
     */
     @Bean
     public AIAssistantService aiAssistantService(OpenAiChatModel chatModel) {
         return AiServices.builder(AIAssistantService.class)
                 .chatLanguageModel(chatModel)
                 .tools(
                     new org.example.langchain4jdemo.tools.BuiltInTools.Calculator(),
                     new org.example.langchain4jdemo.tools.BuiltInTools.DateTime(),
                     new org.example.langchain4jdemo.tools.BuiltInTools.TextProcessor()
                 )
                 // LLM可能出现工具幻觉，调用一个不存在的工具
                 // 配置工具幻觉策略，当出现工具幻觉时，向LLM返回一个响应，告诉他之前调用的工具不存在，希望推动他调用不同的工具
                 .hallucinatedToolNameStrategy(toolExecutionRequest ->
                         ToolExecutionResultMessage.from(toolExecutionRequest, "错误，没有名为 " + toolExecutionRequest.name() + " 的工具"))
                 .build();
     }
}
