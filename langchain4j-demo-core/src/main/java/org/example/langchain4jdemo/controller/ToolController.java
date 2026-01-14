package org.example.langchain4jdemo.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.ToolCallRequestVo;
import org.example.langchain4jdemo.dto.ToolCallResponse;
import org.example.langchain4jdemo.service.ToolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工具调用控制器
 * 演示 LangChain4j 的动态工具调用（ToolSpecification）
 */
@Slf4j
@RestController
@RequestMapping("/api/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /**
     * 带工具调用的聊天
     * POST /api/tool/chat
     *
     * 示例请求：
     * {
     *   "message": "计算 25 + 17 的结果",
     *   "enabledTools": ["calculator"]
     * }
     *
     * 可用工具类型：
     * - calculator: 计算器（加减乘除、平方根、幂运算等）
     * - datetime: 日期时间（当前时间、日期计算、星期判断等）
     * - text: 文本处理（字数统计、大小写转换、反转等）
     * - random: 随机数生成（整数、浮点数、布尔值等）
     * - converter: 单位转换（温度、长度、重量等）
     * - validator: 数据验证（邮箱、URL、手机号等）
     */
    @PostMapping("/chat")
    public ToolCallResponse chatWithTools(@RequestBody ToolCallRequestVo request) {
        log.info("Tool chat request: message={}, tools={}",
                request.getMessage(),
                request.getEnabledTools());

        ToolCallResponse response = toolService.chatWithTools(request);

        log.info("Tool chat response: success={}, content={}, toolExecutions={}",
                response.isSuccess(),
                response.getContent(),
                response.getToolExecutions() != null ? response.getToolExecutions().size() : 0);

        log.info("Returning response to client: {}", response);

        return response;
    }

    /**
     * 获取所有可用的工具列表
     * GET /api/tool/available
     */
    @GetMapping("/available")
    public AvailableToolsResponse getAvailableTools() {
        return new AvailableToolsResponse(List.of(
                new ToolInfo("calculator", "计算器工具（包含加减乘除、平方根、幂运算等）", "数学运算"),
                new ToolInfo("datetime", "日期时间工具（当前时间、日期计算、星期判断等）", "时间处理"),
                new ToolInfo("text", "文本处理工具（字数统计、大小写转换、反转等）", "文本处理"),
                new ToolInfo("random", "随机数生成工具（整数、浮点数、布尔值等）", "随机生成"),
                new ToolInfo("converter", "单位转换工具（温度、长度、重量等）", "单位转换"),
                new ToolInfo("validator", "数据验证工具（邮箱、URL、手机号等）", "数据验证")
        ));
    }

    /**
     * 测试单个工具
     * POST /api/tool/test
     *
     * 示例请求：
     * {
     *   "message": "现在几点了？",
     *   "enabledTools": ["datetime"]
     * }
     */
    @PostMapping("/test")
    public ToolCallResponse testSingleTool(@RequestBody ToolCallRequestVo request) {
        log.info("Testing tool: message={}, tools={}",
                request.getMessage(),
                request.getEnabledTools());

        return toolService.chatWithTools(request);
    }

    // DTO 类

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableToolsResponse {
        private List<ToolInfo> tools;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolInfo {
        private String name;
        private String description;
        private String category;
    }
}
