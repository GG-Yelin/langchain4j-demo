package org.example.langchain4jdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestVO {

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

    /**
     * 温度参数（控制输出随机性，范围 0.0-2.0）
     */
    private Double temperature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem {
        private String role;  // user / assistant
        private String content;
    }
}
