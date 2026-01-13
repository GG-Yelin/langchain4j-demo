package org.example.langchain4jdemo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.service.BuiltInToolService;
import org.springframework.web.bind.annotation.*;

/**
 * 内置工具控制器
 * 演示使用 @Tool 注解定义的工具调用
 */
@Slf4j
@RestController
@RequestMapping("/api/builtin-tools")
@RequiredArgsConstructor
public class BuiltInToolController {

    private final BuiltInToolService builtInToolService;

    /**
     * 使用所有内置工具聊天
     * POST /api/builtin-tools/chat
     */
    @PostMapping("/chat")
    public ToolChatResponse chat(@RequestBody ToolChatRequest request) {
        log.info("Built-in tools chat request: {}", request.getMessage());

        String response = builtInToolService.chatWithAllTools(request.getMessage());

        return new ToolChatResponse(response);
    }

    /**
     * 使用计算器工具
     * POST /api/builtin-tools/calculator
     */
    @PostMapping("/calculator")
    public ToolChatResponse chatWithCalculator(@RequestBody ToolChatRequest request) {
        log.info("Calculator chat request: {}", request.getMessage());

        String response = builtInToolService.chatWithCalculator(request.getMessage());

        return new ToolChatResponse(response);
    }

    /**
     * 使用日期时间工具
     * POST /api/builtin-tools/datetime
     */
    @PostMapping("/datetime")
    public ToolChatResponse chatWithDateTime(@RequestBody ToolChatRequest request) {
        log.info("DateTime chat request: {}", request.getMessage());

        String response = builtInToolService.chatWithDateTime(request.getMessage());

        return new ToolChatResponse(response);
    }

    /**
     * 使用文本处理工具
     * POST /api/builtin-tools/text
     */
    @PostMapping("/text")
    public ToolChatResponse chatWithText(@RequestBody ToolChatRequest request) {
        log.info("Text processor chat request: {}", request.getMessage());

        String response = builtInToolService.chatWithTextProcessor(request.getMessage());

        return new ToolChatResponse(response);
    }

    /**
     * 使用自定义工具组合
     * POST /api/builtin-tools/custom
     */
    @PostMapping("/custom")
    public ToolChatResponse chatWithCustomTools(@RequestBody CustomToolRequest request) {
        log.info("Custom tools chat request: message={}, tools={}",
                request.getMessage(), String.join(", ", request.getToolTypes()));

        String response = builtInToolService.chatWithCustomTools(
                request.getMessage(),
                request.getToolTypes()
        );

        return new ToolChatResponse(response);
    }

    /**
     * 获取可用的工具类型列表
     * GET /api/builtin-tools/available
     */
    @GetMapping("/available")
    public AvailableToolsResponse getAvailableTools() {
        return new AvailableToolsResponse(new String[]{
                "calculator",   // 计算器
                "datetime",     // 日期时间
                "text",         // 文本处理
                "random",       // 随机数生成
                "converter",    // 单位转换
                "validator"     // 数据验证
        });
    }

    // DTO 类

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolChatRequest {
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomToolRequest {
        private String message;
        private String[] toolTypes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolChatResponse {
        private String response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableToolsResponse {
        private String[] tools;
    }
}
