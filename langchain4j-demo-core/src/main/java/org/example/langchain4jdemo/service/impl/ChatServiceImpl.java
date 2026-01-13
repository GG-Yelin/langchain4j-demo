package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ChatRequestVO;
import org.example.langchain4jdemo.dto.ChatResponseVO;
import org.example.langchain4jdemo.service.ChatService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final OpenAiChatModel chatModel;

    @Override
    public ChatResponseVO chat(ChatRequestVO requestVO) {
        try {
            ChatRequest request = ChatRequest.builder()
                    .messages(UserMessage.from(requestVO.getMessage()))
                    .parameters(ChatRequestParameters.builder()
                            .temperature(0.5)
                            .toolSpecifications()
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
    public ChatResponseVO chatWithMemory(ChatRequestVO request) {
        try {
            // TODO: 在这里实现带记忆的多轮对话
            // 提示: 使用 ChatMemory 或 MessageWindowChatMemory
            // 示例:
            // ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
            // 或使用 AiServices 创建带记忆的服务

            throw new UnsupportedOperationException("请实现 chatWithMemory 方法");

        } catch (Exception e) {
            log.error("Chat with memory error", e);
            return ChatResponseVO.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public void streamChat(ChatRequestVO request, StreamCallback callback) {
        try {
            // TODO: 在这里实现流式聊天
            // 提示: 使用 StreamingChatLanguageModel
            // 示例:
            // streamingChatModel.generate(message, new StreamingResponseHandler<>() {
            //     @Override
            //     public void onNext(String token) {
            //         callback.onToken(token);
            //     }
            //     @Override
            //     public void onComplete(Response<AiMessage> response) {
            //         callback.onComplete(...);
            //     }
            //     @Override
            //     public void onError(Throwable error) {
            //         callback.onError(error);
            //     }
            // });

            throw new UnsupportedOperationException("请实现 streamChat 方法");

        } catch (Exception e) {
            log.error("Stream chat error", e);
            callback.onError(e);
        }
    }
}
