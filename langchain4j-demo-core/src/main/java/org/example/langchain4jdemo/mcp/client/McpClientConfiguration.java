package org.example.langchain4jdemo.mcp.client;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.Duration;

/**
 * MCP Client 配置类
 * 使用 LangChain4j 的 MCP Client 连接到 MCP Server
 *
 * 支持两种传输方式：
 * 1. HTTP/SSE (mcp.transport.type=http)
 * 2. Stdio (mcp.transport.type=stdio)
 */
@Slf4j
@Configuration
public class McpClientConfiguration {

    @Value("${mcp.transport.type:stdio}")
    private String transportType;

    @Value("${mcp.server.url:http://localhost:8081}")
    private String mcpServerUrl;

    @Value("${mcp.server.jar:../mcp-server/target/mcp-server-0.0.1-SNAPSHOT.jar}")
    private String mcpServerJar;

    /**
     * 配置 MCP Client
     */
    @Bean
    public McpClient mcpClient() {
        try {
            McpTransport transport = createTransport();

            // 构建 MCP Client
            McpClient client = new DefaultMcpClient.Builder()
                    .transport(transport)
                    .clientName("langchain4j-demo")
                    .clientVersion("1.0.0")
                    .toolExecutionTimeout(Duration.ofSeconds(30))
                    .build();

            log.info("MCP Client initialized successfully with {} transport", transportType);

            return client;

        } catch (Exception e) {
            log.error("Failed to initialize MCP Client", e);
            throw new RuntimeException("Failed to initialize MCP Client", e);
        }
    }

    /**
     * 根据配置创建传输层
     */
    private McpTransport createTransport() {
        if ("stdio".equalsIgnoreCase(transportType)) {
            return createStdioTransport();
        } else if ("http".equalsIgnoreCase(transportType)) {
            return createHttpTransport();
        } else {
            throw new IllegalArgumentException("Unknown transport type: " + transportType +
                    ". Supported: stdio, http");
        }
    }

    /**
     * 创建 Stdio 传输（最简单，推荐）
     */
    private StdioMcpTransport createStdioTransport() {
        log.info("Initializing MCP Client with Stdio transport");
        log.info("MCP Server JAR: {}", mcpServerJar);

        // 验证 JAR 文件存在
        File jarFile = new File(mcpServerJar);
        if (!jarFile.exists()) {
            throw new RuntimeException("MCP Server JAR not found: " + jarFile.getAbsolutePath() +
                    "\nPlease build mcp-server first: cd mcp-server && mvn package -DskipTests");
        }

        return new StdioMcpTransport.Builder()
                .command(java.util.Arrays.asList(
                        "java",
                        "-jar", jarFile.getAbsolutePath()))
                .build();
    }

    /**
     * 创建 HTTP/SSE 传输
     */
    private HttpMcpTransport createHttpTransport() {
        String sseUrl = mcpServerUrl + "/mcp/sse";
        log.info("Initializing MCP Client with HTTP/SSE transport");
        log.info("SSE URL: {}", sseUrl);
        log.warn("Note: HTTP/SSE requires manually starting mcp-server first!");

        return new HttpMcpTransport.Builder()
                .sseUrl(sseUrl)
                .timeout(Duration.ofSeconds(60))
                .build();
    }
}

