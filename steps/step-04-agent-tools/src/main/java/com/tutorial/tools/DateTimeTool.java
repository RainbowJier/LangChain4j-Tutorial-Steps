package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具 — 解决 LLM 不知道当前时间的问题。
 * <p>
 * LLM 的训练数据有截止日期，它不知道"现在"是几点。
 * 通过 @Tool 让 LLM 在需要时主动查询当前时间。
 */
public class DateTimeTool {

    @Tool("获取当前的日期和时间。当用户问"现在几点"、"今天星期几"等问题时使用。")
    public String getCurrentDateTime(
            @P("日期格式，如 'yyyy-MM-dd' 或 'yyyy-MM-dd HH:mm:ss'，默认为完整格式") String format) {
        String pattern = (format == null || format.isBlank())
                ? "yyyy-MM-dd HH:mm:ss"
                : format;
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
