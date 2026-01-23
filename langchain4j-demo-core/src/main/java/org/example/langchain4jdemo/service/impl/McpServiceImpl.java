package org.example.langchain4jdemo.service.impl;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.dto.McpRequest;
import org.example.langchain4jdemo.dto.McpResponse;
import org.example.langchain4jdemo.service.McpService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP (Model Context Protocol) 服务实现
 * 使用 LangChain4j MCP Client 连接远程 MCP Server
 *
 * 功能：
 * 1. chatWithMcp: AI 自动调用 MCP 工具进行对话
 * 2. listAvailableTools: 列出远程 MCP Server 提供的所有工具
 * 3. invokeTool: 直接调用指定的 MCP 工具（通过 MCP 协议远程调用）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpServiceImpl implements McpService {

    private final ChatLanguageModel chatModel;
    private final McpClient mcpClient;

    /**
     * 助手接口，用于 AI 服务
     */
    interface Assistant {
        String chat(String message);
    }

    @Override
    public McpResponse chatWithMcp(McpRequest request) {
        try {
            log.info("Processing MCP chat request: {}", request.getMessage());

            // 创建 MCP 工具提供者
            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClient)
                    .build();

            // 使用 AiServices 构建助手，集成 MCP 工具
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(toolProvider)
                    .build();

            // 调用助手进行对话（AI 会自动通过 MCP 协议调用远程工具）
            String response = assistant.chat(request.getMessage());
            log.info("Assistant response generated successfully");

            return McpResponse.builder()
                    .success(true)
                    .content(response)
                    .build();

        } catch (Exception e) {
            log.error("MCP chat error", e);
            return McpResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public List<Map<String, Object>> listAvailableTools() {
        try {
            log.info("Listing available MCP tools");

            // 通过 MCP Client 列出工具
            List<ToolSpecification> toolSpecs = mcpClient.listTools();

            // 转换为 Map 格式
            List<Map<String, Object>> tools = toolSpecs.stream()
                    .map(spec -> {
                        Map<String, Object> tool = new HashMap<>();
                        tool.put("name", spec.name());
                        tool.put("description", spec.description());
                        if (spec.parameters() != null) {
                            tool.put("parameters", spec.parameters().toString());
                        }
                        return tool;
                    })
                    .collect(Collectors.toList());

            log.info("Found {} MCP tools", tools.size());
            return tools;

        } catch (Exception e) {
            log.error("Failed to list MCP tools", e);
            throw new RuntimeException("Failed to list MCP tools: " + e.getMessage(), e);
        }
    }

    @Override
    public String invokeTool(String toolName, Map<String, Object> parameters) {
        try {
            log.info("========================================");
            log.info("通过 AI 调用 MCP 工具");
            log.info("工具名称: {}", toolName);
            log.info("参数: {}", parameters);
            log.info("========================================");

            // 1. 验证工具是否存在
            List<ToolSpecification> toolSpecs = mcpClient.listTools();
            ToolSpecification toolSpec = toolSpecs.stream()
                    .filter(spec -> spec.name().equals(toolName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tool not found: " + toolName + ". Available tools: " +
                            toolSpecs.stream().map(ToolSpecification::name).collect(Collectors.joining(", "))));

            log.info("工具验证通过: {}", toolName);

            // 2. 构建调用提示词
            String prompt = buildToolInvocationPrompt(toolName, parameters, toolSpec);
            log.info("构建的提示词: {}", prompt);

            // 3. 创建 MCP 工具提供者
            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClient)
                    .build();

            // 4. 使用 AI 助手调用工具（AI 会通过 MCP 协议远程调用）
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatLanguageModel(chatModel)
                    .tools(toolProvider)
                    .build();

            log.info("正在通过 AI 和 MCP 协议远程调用工具...");
            String result = assistant.chat(prompt);

            log.info("========================================");
            log.info("工具调用成功");
            log.info("返回结果: {}", result);
            log.info("========================================");

            return result;

        } catch (IllegalArgumentException e) {
            log.error("工具调用失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("调用 MCP 工具失败: {}", toolName, e);
            throw new RuntimeException("Failed to invoke MCP tool '" + toolName + "': " + e.getMessage(), e);
        }
    }

    /**
     * 构建工具调用提示词
     */
    private String buildToolInvocationPrompt(String toolName, Map<String, Object> parameters, ToolSpecification toolSpec) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please use the '").append(toolName).append("' tool ");
        prompt.append("with the following parameters: ");

        boolean first = true;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (!first) {
                prompt.append(", ");
            }
            first = false;
            prompt.append(entry.getKey()).append(" = ").append(entry.getValue());
        }

        prompt.append(". Return ONLY the tool result without any additional explanation.");

        return prompt.toString();
    }

}
