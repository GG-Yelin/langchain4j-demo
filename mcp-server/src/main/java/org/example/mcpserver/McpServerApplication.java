package org.example.mcpserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * MCP Server 启动入口
 * 使用 Spring AI MCP Server Stdio Starter
 * 工具通过 @McpTool 注解自动扫描注册
 * 通过标准输入输出(stdio)与 LangChain4j MCP Client 通信
 */
@Slf4j
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
