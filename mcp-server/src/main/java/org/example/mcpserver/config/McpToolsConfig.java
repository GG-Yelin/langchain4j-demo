package org.example.mcpserver.config;

import org.example.mcpserver.tools.CalculatorTool;
import org.example.mcpserver.tools.WeatherTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 工具配置
 * 将工具类注册为 MCP Server 可用的工具
 */
@Configuration
public class McpToolsConfig {

    // TODO: 取消注释以注册工具到 MCP Server
    //
    // @Bean
    // public ToolCallbackProvider calculatorTools(CalculatorTool calculatorTool) {
    //     return MethodToolCallbackProvider.builder()
    //         .toolObjects(calculatorTool)
    //         .build();
    // }
    //
    // @Bean
    // public ToolCallbackProvider weatherTools(WeatherTool weatherTool) {
    //     return MethodToolCallbackProvider.builder()
    //         .toolObjects(weatherTool)
    //         .build();
    // }
    //
    // // 如果需要注册 FileSystemTool 和 DatabaseTool:
    // // @Bean
    // // public ToolCallbackProvider fileSystemTools(FileSystemTool fileSystemTool) {
    // //     return MethodToolCallbackProvider.builder()
    // //         .toolObjects(fileSystemTool)
    // //         .build();
    // // }
    // //
    // // @Bean
    // // public ToolCallbackProvider databaseTools(DatabaseTool databaseTool) {
    // //     return MethodToolCallbackProvider.builder()
    // //         .toolObjects(databaseTool)
    // //         .build();
    // // }
}
