package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 计算器工具 — LLM 通过 @Tool 注解发现并调用此方法。
 * <p>
 * 工作原理：
 * 1. LangChain4j 读取 @Tool 注解的描述，告诉 LLM "你有一个计算器可以用"
 * 2. 当用户问 "123 * 456 等于多少" 时，LLM 判断需要调用此工具
 * 3. LLM 输出结构化数据（函数名 + 参数），框架自动调用此 Java 方法
 * 4. 方法返回值被发回 LLM，LLM 基于结果生成自然语言回答
 * <p>
 * 关键注解：
 * - @Tool("描述")：方法描述，给 LLM 看的，帮助它决定何时调用
 * - @P("描述")：参数描述，帮助 LLM 正确传参
 */
public class CalculatorTool {

    @Tool("计算两个数的四则运算结果。当用户需要做数学计算时使用此工具。")
    public String calculate(
            @P("第一个数字") double a,
            @P("运算符：+、-、*、/ 中的一个") String operator,
            @P("第二个数字") double b) {

        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            default -> throw new IllegalArgumentException("不支持的运算符: " + operator);
        };

        return String.format("%.2f %s %.2f = %.2f", a, operator, b, result);
    }
}
