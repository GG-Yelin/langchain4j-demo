package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ChatRequestVO;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.ChatService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final OpenAiChatModel chatModel;
    private final OpenAiStreamingChatModel streamingChatModel;

    // 使用 Map 存储每个 sessionId 对应的 ChatMemory
    private final Map<String, ChatMemory> memoryStore = new ConcurrentHashMap<>();

    @Override
    public ChatResponseVO chat(ChatRequestVO requestVO) {
        try {
            // 使用请求中的 temperature，如果没有提供则使用默认值 0.7
            Double temperature = requestVO.getTemperature() != null ? requestVO.getTemperature() : 0.7;

            ChatRequest request = ChatRequest.builder()
                    .messages(UserMessage.from(requestVO.getMessage()))
                    .parameters(ChatRequestParameters.builder()
                            .temperature(temperature)
                            .build())
                    .build();

            ChatResponse response = chatModel.chat(request);

            return ChatResponseVO.builder()
                    .content(response.aiMessage().text())
                    .tokenUsageVO(ChatResponseVO.TokenUsageVO.from(response.tokenUsage()))
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Chat error", e);
            return ChatResponseVO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public ChatResponseVO chatWithMemory(ChatRequestVO requestVO) {
        try {
            // 获取 sessionId，如果没有则使用默认值
            String sessionId = requestVO.getSessionId() != null ? requestVO.getSessionId() : "default-session";

            // 获取或创建该 session 的 ChatMemory（最多保留 10 条消息）
            ChatMemory chatMemory = memoryStore.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(10));

            // 添加用户消息到记忆中
            UserMessage userMessage = UserMessage.from(requestVO.getMessage());
            chatMemory.add(userMessage);

            // 使用请求中的 temperature，如果没有提供则使用默认值 0.7
            Double temperature = requestVO.getTemperature() != null ? requestVO.getTemperature() : 0.7;

            // 构建请求，包含历史消息
            ChatRequest request = ChatRequest.builder()
                    .messages(chatMemory.messages())  // 使用记忆中的所有消息
                    .parameters(ChatRequestParameters.builder()
                            .temperature(temperature)
                            .build())
                    .build();

            // 调用 AI 模型
            ChatResponse response = chatModel.chat(request);

            // 将 AI 的回复也添加到记忆中
            AiMessage aiMessage = response.aiMessage();
            chatMemory.add(aiMessage);

            log.info("Chat with memory - SessionId: {}, Messages in memory: {}",
                    sessionId, chatMemory.messages().size());

            return ChatResponseVO.builder()
                    .content(aiMessage.text())
                    .sessionId(sessionId)
                    .tokenUsageVO(ChatResponseVO.TokenUsageVO.from(response.tokenUsage()))
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Chat with memory error", e);
            return ChatResponseVO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequestVO requestVO, StreamCallback callback) {
        try {

            // 使用请求中的 temperature，如果没有提供则使用默认值 0.7
            Double temperature = requestVO.getTemperature() != null ? requestVO.getTemperature() : 0.7;


            ChatRequest request = ChatRequest.builder()
                    .messages(UserMessage.from(requestVO.getMessage()))
                    .parameters(ChatRequestParameters.builder()
                            .temperature(temperature)
                            .build())
                    .build();


            streamingChatModel.chat(request, new StreamingChatResponseHandler() {
                 @Override
                 public void onPartialResponse(String token) {
                     // 每接收到一个 token 就回调前端
                     log.debug("Received token: [{}]", token);
                     callback.onToken(token);
                 }

                 @Override
                 public void onCompleteResponse(ChatResponse response) {
                     // 流式传输完成，构建最终的响应对象
                     log.info("Stream chat completed. Full response: {}", response.aiMessage().text());
                     ChatResponseVO responseVO = ChatResponseVO.builder()
                             .content(response.aiMessage().text())
                             .tokenUsageVO(ChatResponseVO.TokenUsageVO.from(response.tokenUsage()))
                             .success(true)
                             .build();
                     callback.onComplete(responseVO);
                 }

                 @Override
                 public void onError(Throwable error) {
                     // 发生错误时回调
                     log.error("Stream chat error in handler", error);
                     callback.onError(error);
                 }
             });


        } catch (Exception e) {
            log.error("Stream chat error", e);
            callback.onError(e);
        }
    }
}
