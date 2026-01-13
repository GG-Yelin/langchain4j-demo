package org.example.langchain4jdemo.controller;

import lombok.RequiredArgsConstructor;
import org.example.langchain4jdemo.dto.ToolCallRequest;
import org.example.langchain4jdemo.dto.ToolCallResponse;
import org.example.langchain4jdemo.service.ToolService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tool")
@RequiredArgsConstructor
public class ToolController {

    private final ToolService toolService;

    /**
     * 带工具调用的聊天
     * POST /api/tool/chat
     */
    @PostMapping("/chat")
    public ToolCallResponse chatWithTools(@RequestBody ToolCallRequest request) {
        return toolService.chatWithTools(request);
    }
}
