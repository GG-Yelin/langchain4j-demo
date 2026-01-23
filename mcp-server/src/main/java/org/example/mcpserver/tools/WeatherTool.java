package org.example.mcpserver.tools;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具
 * 使用 Spring AI MCP @McpTool 注解定义工具方法
 */
@Slf4j
@Component
public class WeatherTool {

    @McpTool(
        name = "weather_get_current",
        description = """
        A tool for getting current weather information for a specific city.
        This tool provides real-time weather data including temperature, conditions, and air quality.
        Supported cities include major Chinese cities like Beijing, Shanghai, Shenzhen, etc.
        """
    )
    public String getWeather(String city) {
        log.info("Weather Tool - Get Current Weather for city: {}", city);

        if (city == null || city.trim().isEmpty()) {
            log.error("City name is empty");
            throw new IllegalArgumentException("City name cannot be empty");
        }

        String weather = switch (city.trim()) {
            case "北京", "Beijing" -> "北京今天晴，温度15-25度，空气质量良好";
            case "上海", "Shanghai" -> "上海今天多云，温度18-26度，湿度较大";
            case "深圳", "Shenzhen" -> "深圳今天阴，温度22-28度，可能有小雨";
            case "广州", "Guangzhou" -> "广州今天晴转多云，温度20-27度";
            case "杭州", "Hangzhou" -> "杭州今天小雨，温度16-22度";
            case "成都", "Chengdu" -> "成都今天阴，温度14-20度，空气湿润";
            default -> city + " 今天天气晴朗，温度适宜";
        };

        log.info("Weather query result: {}", weather);
        return weather;
    }

    @McpTool(
        name = "weather_get_forecast",
        description = """
        A tool for getting weather forecast for a city for the next N days.
        This tool provides multi-day weather predictions including temperature trends.
        The forecast period can be from 1 to 7 days (maximum one week).
        """
    )
    public String getWeatherForecast(String city, int days) {
        log.info("Weather Tool - Get Forecast for city: {}, days: {}", city, days);

        if (city == null || city.trim().isEmpty()) {
            log.error("City name is empty");
            throw new IllegalArgumentException("City name cannot be empty");
        }

        if (days < 1 || days > 7) {
            log.error("Invalid forecast days: {}, must be between 1-7", days);
            throw new IllegalArgumentException(
                String.format("Forecast days must be between 1-7. Provided: %d", days));
        }

        StringBuilder forecast = new StringBuilder();
        forecast.append(city).append(" 未来 ").append(days).append(" 天天气预报：\n");

        for (int i = 1; i <= days; i++) {
            forecast.append(String.format("第 %d 天：晴转多云，温度 %d-%d 度\n",
                i, 15 + i, 25 + i));
        }

        String result = forecast.toString();
        log.info("Forecast result: {}", result);
        return result;
    }
}
