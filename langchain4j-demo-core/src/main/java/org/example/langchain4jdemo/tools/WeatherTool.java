package org.example.langchain4jdemo.tools;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具
 * 提供城市天气信息查询功能
 */
@Slf4j
@Component
public class WeatherTool {

    /**
     * 获取指定城市的天气信息
     *
     * @param city 城市名称，例如: 北京、上海、深圳
     * @return 天气信息描述
     */
    @Tool("获取指定城市的当前天气信息，包括天气状况、温度范围等")
    public String getWeather(String city) {
        log.info("查询城市天气: {}", city);

        // 模拟天气数据（实际应用中应该调用真实的天气 API）
        String weather = switch (city) {
            case "北京" -> "北京今天晴，温度15-25度，空气质量良好";
            case "上海" -> "上海今天多云，温度18-26度，湿度较大";
            case "深圳" -> "深圳今天阴，温度22-28度，可能有小雨";
            case "广州" -> "广州今天晴转多云，温度20-27度";
            case "杭州" -> "杭州今天小雨，温度16-22度";
            case "成都" -> "成都今天阴，温度14-20度，空气湿润";
            default -> city + "今天天气晴朗，温度适宜";
        };

        log.info("天气查询结果: {}", weather);
        return weather;
    }

    /**
     * 获取城市未来几天的天气预报
     *
     * @param city 城市名称
     * @param days 预报天数（1-7天）
     * @return 未来几天的天气预报
     */
    @Tool("获取指定城市未来几天的天气预报")
    public String getWeatherForecast(String city, int days) {
        log.info("查询城市 {} 未来 {} 天的天气预报", city, days);

        if (days < 1 || days > 7) {
            return "预报天数应该在 1-7 天之间";
        }

        StringBuilder forecast = new StringBuilder(city + "未来" + days + "天天气预报：\n");
        for (int i = 1; i <= days; i++) {
            forecast.append(String.format("第%d天：晴转多云，温度%d-%d度\n",
                i, 15 + i, 25 + i));
        }

        return forecast.toString();
    }
}
