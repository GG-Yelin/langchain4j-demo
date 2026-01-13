package org.example.langchain4jdemo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

    /**
     * AI回复的内容
     */
    private String content;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * Token使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    @Data
    @Builder
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
