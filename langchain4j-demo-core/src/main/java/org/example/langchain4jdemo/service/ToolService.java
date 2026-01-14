package org.example.langchain4jdemo.service;

import org.example.langchain4jdemo.dto.ToolCallRequestVo;
import org.example.langchain4jdemo.dto.ToolCallResponse;

/**
 * 工具调用服务接口
 * 用于Function Calling / Tool Use
 */
public interface ToolService {

    /**
     * 带工具调用的聊天
     */
    ToolCallResponse chatWithTools(ToolCallRequestVo request);
}
