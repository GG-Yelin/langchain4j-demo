package org.example.langchain4jdemo;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 流式编码问题调试测试
 * 用于验证 LangChain4j 流式传输中的编码问题
 */
public class StreamingEncodingDebugTest {

    @Test
    public void testStreamingChineseEncoding() throws Exception {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("用中文说：你好世界"))
                .parameters(ChatRequestParameters.builder()
                        .temperature(0.7)
                        .build())
                .build();

        List<String> tokens = new ArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        System.out.println("=== 开始流式传输测试 ===\n");

        model.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                tokens.add(token);
                fullResponse.append(token);

                // 打印每个 token 的详细信息
                System.out.println("Token #" + tokens.size());
                System.out.println("  内容: [" + token + "]");
                System.out.println("  长度: " + token.length() + " 字符");
                System.out.println("  UTF-8 字节数: " + token.getBytes().length);

                // 打印字节码（十六进制）
                byte[] bytes = token.getBytes();
                StringBuilder hex = new StringBuilder();
                for (byte b : bytes) {
                    hex.append(String.format("%02X ", b));
                }
                System.out.println("  字节码: " + hex.toString());

                // 检查是否包含替换字符 (U+FFFD = EF BF BD)
                if (token.contains("�") || token.contains("?")) {
                    System.out.println("  ⚠️ 警告：包含替换字符或问号！");
                }
                System.out.println();
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                System.out.println("=== 流式传输完成 ===");
                System.out.println("总 Token 数: " + tokens.size());
                System.out.println("完整响应: [" + fullResponse.toString() + "]");
                System.out.println("响应长度: " + fullResponse.length() + " 字符");
                System.out.println("\n来自 response.aiMessage().text():");
                System.out.println("[" + response.aiMessage().text() + "]");
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("流式传输错误: " + error.getMessage());
                error.printStackTrace();
            }
        });

        // 等待流式传输完成
        Thread.sleep(10000);
    }

    @Test
    public void testStreamingEnglishEncoding() throws Exception {
        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("Say: Hello World"))
                .parameters(ChatRequestParameters.builder()
                        .temperature(0.7)
                        .build())
                .build();

        List<String> tokens = new ArrayList<>();
        StringBuilder fullResponse = new StringBuilder();

        System.out.println("=== 开始英文流式传输测试 ===\n");

        model.chat(request, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String token) {
                tokens.add(token);
                fullResponse.append(token);

                System.out.println("Token #" + tokens.size() + ": [" + token + "]");
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                System.out.println("\n=== 英文流式传输完成 ===");
                System.out.println("总 Token 数: " + tokens.size());
                System.out.println("拼接结果: [" + fullResponse.toString() + "]");
                System.out.println("完整响应: [" + response.aiMessage().text() + "]");

                // 检查是否有空格 token
                long spaceTokens = tokens.stream().filter(t -> t.trim().isEmpty()).count();
                System.out.println("空格/空白 Token 数: " + spaceTokens);
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("流式传输错误: " + error.getMessage());
                error.printStackTrace();
            }
        });

        // 等待流式传输完成
        Thread.sleep(10000);
    }
}
