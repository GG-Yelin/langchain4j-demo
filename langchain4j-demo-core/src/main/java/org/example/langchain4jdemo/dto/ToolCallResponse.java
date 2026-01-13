package org.example.langchain4jdemo.dto;

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
}
