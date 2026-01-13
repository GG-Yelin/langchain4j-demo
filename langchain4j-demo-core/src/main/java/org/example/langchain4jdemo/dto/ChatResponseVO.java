package org.example.langchain4jdemo.dto;

import dev.langchain4j.model.output.TokenUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class ChatResponseVO {

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
    private TokenUsageVO tokenUsageVO;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsageVO {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;

        public static TokenUsageVO from(TokenUsage tokenUsage) {
            TokenUsageVO vo = new TokenUsageVO();
            vo.inputTokens = tokenUsage.inputTokenCount();
            vo.outputTokens = tokenUsage.outputTokenCount();
            vo.totalTokens = tokenUsage.totalTokenCount();
            return vo;
        }

    }
}
