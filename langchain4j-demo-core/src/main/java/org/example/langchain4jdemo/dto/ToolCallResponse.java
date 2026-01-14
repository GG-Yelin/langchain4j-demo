package org.example.langchain4jdemo.dto;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ToolCallResponse {

    /**
     * 最终回复
     */
    private String content;

    /**
     * 调用的工具列表
     */
    private List<ToolExecution> toolExecutions;

    private boolean success;
    private String errorMessage;

    /**
     * Token 使用情况
     */
    private TokenUsage tokenUsage;

    @Data
    @Builder
    public static class ToolExecution {
        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具参数
         */
        private String arguments;

        /**
         * 执行结果
         */
        private String result;

    }

    @Data
    @Builder
    public static class TokenUsage {
        /**
         * 输入token数量
         */
        private Integer inputTokens;

        /**
         * 输出token数量
         */
        private Integer outputTokens;

        /**
         * 总token数量
         */
        private Integer totalTokens;
    }
}
