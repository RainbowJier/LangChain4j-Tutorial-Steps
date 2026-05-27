package com.tutorial;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Step 05: 会话记忆 — 让 LLM 记住对话历史。
 * <p>
 * 前面步骤的问题：每次调用 chat() 都是独立的，LLM 不记得之前说过什么。
 * <p>
 * 解决方案：
 * - ChatMemory：存储对话历史（用户消息 + AI 回复）
 * - MessageWindowChatMemory：滑动窗口，只保留最近 N 条消息
 * - @MemoryId：区分不同用户的会话
 * - ChatMemoryProvider：为每个会话 ID 创建独立的 ChatMemory
 * <p>
 * 核心权衡：
 * - 更多记忆 → 更好的上下文理解，但更高的 Token 消耗
 * - 更少记忆 → 省钱，但可能丢失重要上下文
 */
public class MemoryDemo {

    @SystemMessage("你是一个友好的助手。记住用户告诉你的信息。")
    interface MemoryAssistant {
        String chat(@MemoryId String sessionId, @UserMessage String message);
    }

    public static void main(String[] args) {
        ChatModel chatModel = createChatModel();
        System.out.println("ChatModel 创建成功\n");

        // ===== 方案 A：无记忆（回顾 Step 01 的问题） =====
        System.out.println("=== 方案 A：无记忆（问题演示） ===");

        MemoryAssistant noMemory = AiServices.builder(MemoryAssistant.class)
                .chatModel(chatModel)
                .build();

        String r1 = noMemory.chat("demo", "我叫小明");
        System.out.println("第 1 句: 我叫小明");
        System.out.println("AI 回复: " + r1);

        String r2 = noMemory.chat("demo", "我叫什么名字？");
        System.out.println("第 2 句: 我叫什么名字？");
        System.out.println("AI 回复: " + r2);
        System.out.println("↑ 注意：AI 可能不记得你的名字（无记忆）\n");

        // ===== 方案 B：有记忆（本步核心） =====
        System.out.println("=== 方案 B：有记忆（ChatMemory） ===");

        // ChatMemoryProvider：为每个 sessionId 创建独立的 ChatMemory
        // 这里用 InMemoryChatMemoryStore（内存存储，重启丢失）
        // maxMessages=20：保留最近 20 条消息（10 轮对话）
        ChatMemoryProvider memoryProvider = new ChatMemoryProvider() {
            private final InMemoryChatMemoryStore store = new InMemoryChatMemoryStore();

            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId.toString())
                        .chatMemoryStore(store)
                        .maxMessages(20)
                        .build();
            }
        };

        MemoryAssistant withMemory = AiServices.builder(MemoryAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryProvider)   // ← 关键：注入记忆
                .build();

        // 会话 1（用户 A）
        System.out.println("--- 会话 1（用户 A）---");
        String a1 = withMemory.chat("user-A", "我叫小明，我是一名 Java 开发者");
        System.out.println("用户 A: 我叫小明，我是一名 Java 开发者");
        System.out.println("AI: " + a1);

        String a2 = withMemory.chat("user-A", "我叫什么？我是做什么的？");
        System.out.println("用户 A: 我叫什么？我是做什么的？");
        System.out.println("AI: " + a2);
        System.out.println("↑ AI 记住了用户 A 的信息\n");

        // 会话 2（用户 B，独立会话）
        System.out.println("--- 会话 2（用户 B，独立会话）---");
        String b1 = withMemory.chat("user-B", "我叫小红，我是前端工程师");
        System.out.println("用户 B: 我叫小红，我是前端工程师");
        System.out.println("AI: " + b1);

        String b2 = withMemory.chat("user-B", "我叫什么？");
        System.out.println("用户 B: 我叫什么？");
        System.out.println("AI: " + b2);
        System.out.println("↑ 用户 B 的会话独立，不会混淆用户 A 的信息\n");

        // ===== 交互式对话 =====
        System.out.println("=== 交互式对话（有记忆） ===");
        System.out.println("输入问题开始对话，输入 exit 退出\n");

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
                String response = withMemory.chat("interactive", question);
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
        InputStream is = MemoryDemo.class.getClassLoader()
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
