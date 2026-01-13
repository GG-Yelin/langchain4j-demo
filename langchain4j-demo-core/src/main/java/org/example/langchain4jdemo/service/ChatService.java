package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.ChatRequest;
import org.example.langchain4jdemo.dto.ChatResponse;

/**
 * 聊天服务接口
 * 用于简单对话和多轮对话
 */
public interface ChatService {

    /**
     * 简单聊天 - 单轮对话
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 带记忆的聊天 - 多轮对话
     */
    ChatResponse chatWithMemory(ChatRequest request);

    /**
     * 流式聊天（返回的内容会逐步生成）
     * 注意：实际实现时需要配合SSE或WebSocket
     */
    void streamChat(ChatRequest request, StreamCallback callback);

    /**
     * 流式回调接口
     */
    interface StreamCallback {
        void onToken(String token);
        void onComplete(ChatResponse response);
        void onError(Throwable error);
    }
}
