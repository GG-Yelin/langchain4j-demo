package org.example.langchain4jdemo.mcp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Client 配置类
 * 通过 HTTP/SSE 连接到独立运行的 MCP Server
 */
@Configuration
public class McpClientConfiguration {

    @Value("${mcp.server.base-url:http://localhost:8081}")
    private String mcpServerBaseUrl;

    // TODO: 配置 MCP Client Bean 连接到 MCP Server (HTTP/SSE 模式)
    //
    // ============================================
    // HTTP/SSE 模式连接 MCP Server
    // ============================================
    //
    // @Bean
    // public McpClient mcpClient() {
    //     // 使用 HTTP SSE 传输连接到 MCP Server
    //     McpTransport transport = new HttpMcpTransport.Builder()
    //         .baseUrl(mcpServerBaseUrl)           // MCP Server 地址
    //         .sseEndpoint("/sse")                 // SSE 端点
    //         .build();
    //
    //     McpClient client = new DefaultMcpClient.Builder()
    //         .transport(transport)
    //         .build();
    //
    //     client.initialize();
    //     return client;
    // }
    //
    // ============================================
    // 创建 McpToolProvider 简化工具集成
    // ============================================
    //
    // @Bean
    // public McpToolProvider mcpToolProvider(McpClient mcpClient) {
    //     return McpToolProvider.builder()
    //         .mcpClients(List.of(mcpClient))
    //         .build();
    // }
    //
    // ============================================
    // 使用示例（在 Service 中）
    // ============================================
    //
    // interface Assistant {
    //     String chat(String message);
    // }
    //
    // @Bean
    // public Assistant assistant(ChatLanguageModel chatModel, McpToolProvider toolProvider) {
    //     return AiServices.builder(Assistant.class)
    //         .chatLanguageModel(chatModel)
    //         .toolProvider(toolProvider)
    //         .build();
    // }
}
