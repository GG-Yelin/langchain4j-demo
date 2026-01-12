package org.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 天气查询工具
 * 使用 Spring AI 的 @Tool 注解定义 MCP 工具
 */
@Service
public class WeatherTool {

    // TODO: 实现天气查询工具
    // 可以调用真实的天气API，这里用模拟数据演示
    //
    // @Tool(description = "获取指定城市的当前天气")
    // public String getWeather(
    //     @ToolParam(description = "城市名称，如: 北京、上海、广州") String city) {
    //     // 模拟数据，实际可调用天气API
    //     return String.format("""
    //         城市: %s
    //         温度: 25°C
    //         天气: 晴朗
    //         湿度: 60%%
    //         风力: 东南风3级
    //         """, city);
    // }
    //
    // @Tool(description = "获取未来几天的天气预报")
    // public String getForecast(
    //     @ToolParam(description = "城市名称") String city,
    //     @ToolParam(description = "预报天数，1-7天") int days) {
    //     if (days < 1 || days > 7) {
    //         throw new IllegalArgumentException("天数必须在1-7之间");
    //     }
    //     StringBuilder sb = new StringBuilder();
    //     sb.append(city).append(" 未来 ").append(days).append(" 天天气预报:\n\n");
    //     String[] weathers = {"晴", "多云", "阴", "小雨", "晴转多云"};
    //     for (int i = 1; i <= days; i++) {
    //         sb.append("第 ").append(i).append(" 天: ")
    //           .append(weathers[i % weathers.length])
    //           .append(", ").append(18 + i).append("-").append(25 + i).append("°C\n");
    //     }
    //     return sb.toString();
    // }
}
