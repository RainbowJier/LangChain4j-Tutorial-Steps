package com.tutorial;

import com.tutorial.tools.CalculatorTool;
import com.tutorial.tools.DateTimeTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

/**
 * Step 04: Agent 工具调用 — 让 LLM 调用你写的 Java 方法。
 * <p>
 * 核心概念：
 * - @Tool：标注一个方法为"工具"，LLM 可以调用它
 * - @P：描述参数含义，帮助 LLM 正确传参
 * - LLM 自动决定何时调用哪个工具（Function Calling）
 * <p>
 * 工作流程：
 * 1. 用户问 "123 * 456 等于多少"
 * 2. LLM 判断需要调用 calculate 工具，输出：{ "a": 123, "operator": "*", "b": 456 }
 * 3. LangChain4j 框架自动调用 CalculatorTool.calculate(123, "*", 456)
 * 4. 返回值 "123.00 * 456.00 = 56088.00" 发回 LLM
 * 5. LLM 生成自然语言回答："123 乘以 456 等于 56088"
 */
public class ToolDemo {

    @SystemMessage("""
            你是一个智能助手，可以帮用户做计算和查询时间。
            当用户问计算题时，使用计算器工具。
            当用户问时间相关问题时，使用日期时间工具。
            对于普通问题，直接回答即可。
            """)
    interface ToolAssistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        // 1. 创建 ChatModel
        ChatModel chatModel = createChatModel();
        System.out.println("ChatModel 创建成功\n");

        // 2. 创建工具实例
        CalculatorTool calcTool = new CalculatorTool();
        DateTimeTool dateTimeTool = new DateTimeTool();

        // 3. 组装 AiServices（关键：注册工具）
        ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                .chatModel(chatModel)
                .tools(calcTool, dateTimeTool)   // ← 注册工具
                .build();
        System.out.println("AI 服务组装完成（已注册：CalculatorTool、DateTimeTool）\n");

        // ===== 演示：触发工具调用 =====
        System.out.println("=== 工具调用演示 ===\n");

        // 演示 1：计算器工具
        System.out.println("[演示 1] 触发计算器工具");
        String answer1 = assistant.chat("123 * 456 等于多少？");
        System.out.println("答: " + answer1);
        System.out.println();

        // 演示 2：日期时间工具
        System.out.println("[演示 2] 触发日期时间工具");
        String answer2 = assistant.chat("现在几点了？");
        System.out.println("答: " + answer2);
        System.out.println();

        // 演示 3：不触发工具（普通对话）
        System.out.println("[演示 3] 不触发工具（普通对话）");
        String answer3 = assistant.chat("你好，介绍一下你自己");
        System.out.println("答: " + answer3);
        System.out.println();

        // ===== 交互式对话 =====
        System.out.println("=== 交互式对话 ===");
        System.out.println("试试以下问题：");
        System.out.println("  - (15 + 27) * 3 等于多少？（触发计算器）");
        System.out.println("  - 今天星期几？（触发日期工具）");
        System.out.println("  - 你好（不触发任何工具）");
        System.out.println("输入 exit 退出\n");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("你: ");
            String question = scanner.nextLine().trim();
            if (question.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(question)) {
                System.out.println("再见！");
                break;
            }
            try {
                String response = assistant.chat(question);
                System.out.println("助手: " + response);
                System.out.println();
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static ChatModel createChatModel() {
        Yaml yaml = new Yaml();
        InputStream is = ToolDemo.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml 未找到");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> config = yaml.load(is);
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            System.err.println("请先配置 API Key！参见 step-00-setup");
            System.exit(1);
        }

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }
}
