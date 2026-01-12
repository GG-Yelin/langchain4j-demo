package org.example.langchain4jdemo.mcp.client;

import org.springframework.context.annotation.Configuration;

/**
 * MCP Client 配置类
 * 配置连接到独立运行的 MCP Server
 */
@Configuration
public class McpClientConfiguration {

    // TODO: 配置 MCP Client Bean 连接到独立的 MCP Server
    //
    // ============================================
    // 方式1: 连接到 Stdio MCP Server（推荐）
    // ============================================
    // MCP Server 作为子进程启动，通过标准输入输出通信
    //
    // @Bean
    // public McpClient mcpClient() {
    //     // 指定 MCP Server JAR 的路径
    //     String mcpServerJar = "/path/to/mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar";
    //
    //     McpTransport transport = new StdioMcpTransport.Builder()
    //         .command(List.of("java", "-jar", mcpServerJar))
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
    // 方式2: 连接到第三方 MCP Server
    // ============================================
    // 例如: 文件系统、GitHub 等官方 MCP Server
    //
    // @Bean
    // public McpClient npmMcpClient() {
    //     McpTransport transport = new StdioMcpTransport.Builder()
    //         .command(List.of("npx", "-y", "@modelcontextprotocol/server-filesystem", "/allowed/path"))
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
    // 方式3: 创建 McpToolProvider 简化工具集成
    // ============================================
    // 将 MCP 工具自动注入到 AiServices
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
