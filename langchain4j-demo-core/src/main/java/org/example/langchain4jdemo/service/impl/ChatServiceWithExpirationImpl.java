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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带自动过期功能的聊天服务实现
 *
 * 特性：
 * 1. 记忆会在指定时间后自动过期
 * 2. 定时清理过期的会话
 * 3. 支持手动清理
 */
@Slf4j
@Service("chatServiceWithExpiration")
@RequiredArgsConstructor
public class ChatServiceWithExpirationImpl implements ChatService {

    private final OpenAiChatModel chatModel;
    private final OpenAiStreamingChatModel streamingChatModel;

    // 会话过期时间：30分钟（可配置）
    private static final long SESSION_EXPIRATION_MS = 30 * 60 * 1000;

    // 存储会话及其最后访问时间
    private static class SessionData {
        ChatMemory memory;
        Instant lastAccessTime;

        SessionData(ChatMemory memory) {
            this.memory = memory;
            this.lastAccessTime = Instant.now();
        }

        void updateAccessTime() {
            this.lastAccessTime = Instant.now();
        }

        boolean isExpired() {
            return Instant.now().toEpochMilli() - lastAccessTime.toEpochMilli() > SESSION_EXPIRATION_MS;
        }
    }

    private final Map<String, SessionData> memoryStore = new ConcurrentHashMap<>();

    /**
     * 定时清理过期会话（每10分钟执行一次）
     */
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanupExpiredSessions() {
        log.info("Starting cleanup of expired sessions...");

        int removedCount = 0;
        for (Map.Entry<String, SessionData> entry : memoryStore.entrySet()) {
            if (entry.getValue().isExpired()) {
                memoryStore.remove(entry.getKey());
                removedCount++;
                log.debug("Removed expired session: {}", entry.getKey());
            }
        }

        if (removedCount > 0) {
            log.info("Cleanup completed. Removed {} expired sessions. Remaining: {}",
                    removedCount, memoryStore.size());
        } else {
            log.debug("Cleanup completed. No expired sessions found. Total sessions: {}",
                    memoryStore.size());
        }
    }

    @Override
    public ChatResponseVO chat(ChatRequestVO requestVO) {
        try {
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
            String sessionId = requestVO.getSessionId() != null ? requestVO.getSessionId() : "default-session";

            // 获取或创建会话数据
            SessionData sessionData = memoryStore.computeIfAbsent(sessionId,
                id -> {
                    log.info("Creating new session: {}", id);
                    return new SessionData(MessageWindowChatMemory.withMaxMessages(10));
                });

            // 更新最后访问时间
            sessionData.updateAccessTime();

            // 添加用户消息
            UserMessage userMessage = UserMessage.from(requestVO.getMessage());
            sessionData.memory.add(userMessage);

            Double temperature = requestVO.getTemperature() != null ? requestVO.getTemperature() : 0.7;

            ChatRequest request = ChatRequest.builder()
                    .messages(sessionData.memory.messages())
                    .parameters(ChatRequestParameters.builder()
                            .temperature(temperature)
                            .build())
                    .build();

            ChatResponse response = chatModel.chat(request);

            AiMessage aiMessage = response.aiMessage();
            sessionData.memory.add(aiMessage);

            log.info("Chat with memory - SessionId: {}, Messages: {}, LastAccess: {}",
                    sessionId, sessionData.memory.messages().size(), sessionData.lastAccessTime);

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
                    log.debug("Received token: [{}]", token);
                    callback.onToken(token);
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
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
                    log.error("Stream chat error in handler", error);
                    callback.onError(error);
                }
            });

        } catch (Exception e) {
            log.error("Stream chat error", e);
            callback.onError(e);
        }
    }

    @Override
    public void clearMemory(String sessionId) {
        if (sessionId != null && memoryStore.containsKey(sessionId)) {
            memoryStore.remove(sessionId);
            log.info("Manually cleared memory for session: {}", sessionId);
        } else {
            log.warn("Session not found: {}", sessionId);
        }
    }

    @Override
    public void clearAllMemory() {
        int count = memoryStore.size();
        memoryStore.clear();
        log.info("Manually cleared all memory. Total sessions removed: {}", count);
    }

    @Override
    public int getMemorySessionCount() {
        return memoryStore.size();
    }
}
