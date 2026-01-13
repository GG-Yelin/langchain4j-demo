package org.example.langchain4jdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    /**
     * 用户输入的消息
     */
    private String message;

    /**
     * 会话ID（用于多轮对话）
     */
    private String sessionId;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 历史消息列表（可选）
     */
    private List<MessageItem> history;

    @Data
    public static class MessageItem {
        private String role;  // user / assistant
        private String content;
    }
}
