package org.example.langchain4jdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ToolCallRequestVo {

    /**
     * 用户消息
     */
    private String message;

    /**
     * 可用的工具名称列表
     */
    private List<String> enabledTools;
}
