package org.example.langchain4jdemo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class McpResponse {

    /**
     * AI回复内容
     */
    private String content;

    /**
     * 调用的MCP工具列表
     */
    private List<McpToolExecution> toolExecutions;

    private boolean success;
    private String errorMessage;

    @Data
    @Builder
    public static class McpToolExecution {
        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具描述
         */
        private String description;

        /**
         * 输入参数
         */
        private String input;

        /**
         * 执行结果
         */
        private String output;
    }
}
