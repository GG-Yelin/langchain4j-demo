package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.McpRequest;
import org.example.langchain4jdemo.dto.McpResponse;

import java.util.List;
import java.util.Map;

/**
 * MCP (Model Context Protocol) 服务接口
 * 用于连接和使用MCP服务器提供的工具
 */
public interface McpService {

    /**
     * 使用MCP工具进行聊天
     */
    McpResponse chatWithMcp(McpRequest request);

    /**
     * 获取MCP服务器提供的工具列表
     */
    List<Map<String, Object>> listAvailableTools();

    /**
     * 直接调用指定的MCP工具
     */
    String invokeTool(String toolName, Map<String, Object> parameters);
}
