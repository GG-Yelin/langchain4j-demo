package org.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 计算器工具
 * 使用 Spring AI 的 @Tool 注解定义 MCP 工具
 */
@Service
public class CalculatorTool {

    // TODO: 实现计算器工具
    //
    // @Tool(description = "计算两个数的和")
    // public int add(
    //     @ToolParam(description = "第一个数") int a,
    //     @ToolParam(description = "第二个数") int b) {
    //     return a + b;
    // }
    //
    // @Tool(description = "计算两个数的差")
    // public int subtract(
    //     @ToolParam(description = "被减数") int a,
    //     @ToolParam(description = "减数") int b) {
    //     return a - b;
    // }
    //
    // @Tool(description = "计算两个数的积")
    // public int multiply(
    //     @ToolParam(description = "第一个数") int a,
    //     @ToolParam(description = "第二个数") int b) {
    //     return a * b;
    // }
    //
    // @Tool(description = "计算两个数的商")
    // public double divide(
    //     @ToolParam(description = "被除数") double a,
    //     @ToolParam(description = "除数") double b) {
    //     if (b == 0) {
    //         throw new IllegalArgumentException("除数不能为0");
    //     }
    //     return a / b;
    // }
}
