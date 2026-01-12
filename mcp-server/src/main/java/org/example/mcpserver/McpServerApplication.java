package org.example.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP Server 启动入口
 *
 * 基于 Spring AI MCP Server Starter
 *
 * 启动方式:
 * 1. mvn spring-boot:run
 * 2. java -jar target/mcp-server-0.0.1-SNAPSHOT.jar
 *
 * 默认使用 Stdio 传输模式，适合被其他进程调用
 */
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
