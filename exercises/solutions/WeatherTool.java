package com.tutorial.exercises;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 练习解答：天气工具
 * <p>
 * 对应步骤：Step 04 - Agent 工具调用
 * 题目：创建一个返回城市天气的工具（不需要真实 API）
 */
public class WeatherTool {

    @Tool("查询指定城市的天气信息。当用户问天气、温度、是否下雨等问题时使用。")
    public String getWeather(
            @P("城市名称，如'北京'、'上海'、'深圳'") String city) {
        // 模拟天气数据（实际项目中应调用天气 API）
        return switch (city) {
            case "北京" -> """
                    北京天气：
                    - 天气: 晴
                    - 温度: 26°C
                    - 湿度: 45%
                    - 风力: 北风 3 级
                    """;
            case "上海" -> """
                    上海天气：
                    - 天气: 多云
                    - 温度: 28°C
                    - 湿度: 72%
                    - 风力: 东南风 2 级
                    """;
            case "深圳" -> """
                    深圳天气：
                    - 天气: 雷阵雨
                    - 温度: 31°C
                    - 湿度: 85%
                    - 风力: 南风 4 级
                    """;
            default -> city + "的天气暂无数据（模拟数据仅支持北京、上海、深圳）";
        };
    }
}
