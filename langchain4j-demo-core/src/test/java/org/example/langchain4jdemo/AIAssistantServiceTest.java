package org.example.langchain4jdemo;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import org.example.langchain4jdemo.service.AIAssistantService;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * AI 助手服务测试
 * 验证 Result<String> 返回类型和 TokenUsage
 */
public class AIAssistantServiceTest {

    @Test
    public void testChatWithTokenUsage() {
        // 创建 OpenAI Chat Model
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        // 使用 AiServices.create() 创建 AIAssistantService 实现
        AIAssistantService service = AiServices.create(AIAssistantService.class, chatModel);

        // 测试简单聊天
        System.out.println("\n=== 测试简单聊天 ===");
        Result<String> result1 = service.chat("Hello, how are you?");
        System.out.println("响应内容: " + result1.content());
        System.out.println("Token 使用: " + result1.tokenUsage());
        if (result1.tokenUsage() != null) {
            System.out.println("  输入 tokens: " + result1.tokenUsage().inputTokenCount());
            System.out.println("  输出 tokens: " + result1.tokenUsage().outputTokenCount());
            System.out.println("  总计 tokens: " + result1.tokenUsage().totalTokenCount());
        }

        // 测试自定义系统提示词
        System.out.println("\n=== 测试自定义系统提示词 ===");
        Result<String> result2 = service.chatWithSystemMessage(
                "You are a professional translator.",
                "Translate 'Hello' to French"
        );
        System.out.println("响应内容: " + result2.content());
        System.out.println("Token 使用: " + result2.tokenUsage());
        if (result2.tokenUsage() != null) {
            System.out.println("  输入 tokens: " + result2.tokenUsage().inputTokenCount());
            System.out.println("  输出 tokens: " + result2.tokenUsage().outputTokenCount());
            System.out.println("  总计 tokens: " + result2.tokenUsage().totalTokenCount());
        }

        // 测试变量模板
        System.out.println("\n=== 测试变量模板 ===");
        Result<String> result3 = service.chatWithVariables("Chinese", "artificial intelligence");
        System.out.println("响应内容: " + result3.content());
        System.out.println("Token 使用: " + result3.tokenUsage());
        if (result3.tokenUsage() != null) {
            System.out.println("  输入 tokens: " + result3.tokenUsage().inputTokenCount());
            System.out.println("  输出 tokens: " + result3.tokenUsage().outputTokenCount());
            System.out.println("  总计 tokens: " + result3.tokenUsage().totalTokenCount());
        }
    }
}
