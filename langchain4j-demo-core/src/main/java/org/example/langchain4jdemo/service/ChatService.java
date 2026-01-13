package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.ChatRequestVO;
import org.example.langchain4jdemo.dto.ChatResponseVO;

/**
 * 聊天服务接口
 * 用于简单对话和多轮对话
 */
public interface ChatService {

    /**
     * 简单聊天 - 单轮对话
     */
    ChatResponseVO chat(ChatRequestVO request);

    /**
     * 带记忆的聊天 - 多轮对话
     */
    ChatResponseVO chatWithMemory(ChatRequestVO request);

    /**
     * 流式聊天（返回的内容会逐步生成）
     * 注意：实际实现时需要配合SSE或WebSocket
     */
    void streamChat(ChatRequestVO request, StreamCallback callback);

    /**
     * 流式回调接口
     */
    interface StreamCallback {
        void onToken(String token);
        void onComplete(ChatResponseVO response);
        void onError(Throwable error);
    }
}
