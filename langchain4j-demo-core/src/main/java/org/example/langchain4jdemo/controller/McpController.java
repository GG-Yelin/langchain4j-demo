package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.langchain4jdemo.dto.McpRequest;
import org.example.langchain4jdemo.dto.McpResponse;
import org.example.langchain4jdemo.service.McpService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    /**
     * 使用MCP工具进行聊天
     * POST /api/mcp/chat
     */
    @PostMapping("/chat")
    public McpResponse chatWithMcp(@RequestBody McpRequest request) {
        return mcpService.chatWithMcp(request);
    }

    /**
     * 获取可用的MCP工具列表
     * GET /api/mcp/tools
     */
    @GetMapping("/tools")
    public List<Map<String, Object>> listTools() {
        return mcpService.listAvailableTools();
    }

    /**
     * 直接调用MCP工具
     * POST /api/mcp/invoke
     */
    @PostMapping("/invoke")
    public String invokeTool(
            @RequestParam String toolName,
            @RequestBody Map<String, Object> parameters) {
        return mcpService.invokeTool(toolName, parameters);
    }
}
