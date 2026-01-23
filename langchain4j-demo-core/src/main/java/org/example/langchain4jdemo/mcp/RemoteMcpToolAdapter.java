package org.example.langchain4jdemo.mcp;

import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.langchain4jdemo.mcp.client.RemoteMcpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程 MCP 工具适配器
 * 将远程 MCP Server 的工具适配为 LangChain4j 的工具
 */
@Slf4j
@RequiredArgsConstructor
public class RemoteMcpToolAdapter {

    private final RemoteMcpClient remoteMcpClient;

    // ==================== 天气工具 ====================

    @Tool("Get current weather information for a city")
    public String getWeather(String city) {
        log.info("Calling remote getWeather tool for city: {}", city);
        Map<String, Object> args = new HashMap<>();
        args.put("city", city);
        return remoteMcpClient.executeTool("getWeather", args);
    }

    @Tool("Get weather forecast for a city for specified days")
    public String getWeatherForecast(String city, int days) {
        log.info("Calling remote getWeatherForecast tool for city: {}, days: {}", city, days);
        Map<String, Object> args = new HashMap<>();
        args.put("city", city);
        args.put("days", days);
        return remoteMcpClient.executeTool("getWeatherForecast", args);
    }

    // ==================== 计算器工具 ====================

    @Tool("Add two numbers")
    public double add(double a, double b) {
        log.info("Calling remote add tool: {} + {}", a, b);
        Map<String, Object> args = new HashMap<>();
        args.put("a", a);
        args.put("b", b);
        String result = remoteMcpClient.executeTool("add", args);
        return Double.parseDouble(result);
    }

    @Tool("Subtract two numbers")
    public double subtract(double a, double b) {
        log.info("Calling remote subtract tool: {} - {}", a, b);
        Map<String, Object> args = new HashMap<>();
        args.put("a", a);
        args.put("b", b);
        String result = remoteMcpClient.executeTool("subtract", args);
        return Double.parseDouble(result);
    }

    @Tool("Multiply two numbers")
    public double multiply(double a, double b) {
        log.info("Calling remote multiply tool: {} * {}", a, b);
        Map<String, Object> args = new HashMap<>();
        args.put("a", a);
        args.put("b", b);
        String result = remoteMcpClient.executeTool("multiply", args);
        return Double.parseDouble(result);
    }

    @Tool("Divide two numbers")
    public double divide(double a, double b) {
        log.info("Calling remote divide tool: {} / {}", a, b);
        Map<String, Object> args = new HashMap<>();
        args.put("a", a);
        args.put("b", b);
        String result = remoteMcpClient.executeTool("divide", args);
        return Double.parseDouble(result);
    }

    @Tool("Calculate power of a number")
    public double power(double base, double exponent) {
        log.info("Calling remote power tool: {} ^ {}", base, exponent);
        Map<String, Object> args = new HashMap<>();
        args.put("base", base);
        args.put("exponent", exponent);
        String result = remoteMcpClient.executeTool("power", args);
        return Double.parseDouble(result);
    }

    @Tool("Calculate square root of a number")
    public double sqrt(double number) {
        log.info("Calling remote sqrt tool: √{}", number);
        Map<String, Object> args = new HashMap<>();
        args.put("number", number);
        String result = remoteMcpClient.executeTool("sqrt", args);
        return Double.parseDouble(result);
    }
}
