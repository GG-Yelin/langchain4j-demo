package org.example.langchain4jdemo.dto;

import lombok.Data;

import java.util.Map;

@Data
public class McpRequest {

    /**
     * 用户消息
     */
    private String message;

    /**
     * MCP服务器地址
     */
    private String serverUrl;

    /**
     * 工具参数
     */
    private Map<String, Object> toolParameters;
}
