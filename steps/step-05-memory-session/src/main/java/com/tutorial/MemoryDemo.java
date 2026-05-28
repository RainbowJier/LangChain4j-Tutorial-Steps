package com.tutorial;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

/**
 * Step 05: Session Memory — Let LLM remember conversation history.
 * <p>
 * Problem from previous steps: Each call to chat() is independent, LLM doesn't remember what was said before.
 * <p>
 * Solution:
 * - ChatMemory: Store conversation history (user messages + AI replies)
 * - MessageWindowChatMemory: Sliding window, keeps only recent N messages
 * - @MemoryId: Distinguish different users' sessions
 * - ChatMemoryProvider: Create independent ChatMemory for each session ID
 * <p>
 * Core trade-off:
 * - More memory → Better context understanding, but higher token consumption
 * - Less memory → Save money, but may lose important context
 */
public class MemoryDemo {

    @SystemMessage("You are a friendly assistant.")
    interface NoMemoryAssistant {
        String chat(@UserMessage String message);
    }

    @SystemMessage("You are a friendly assistant. Remember the information users tell you.")
    interface MemoryAssistant {
        String chat(@MemoryId String sessionId, @UserMessage String message);
    }

    public static void main(String[] args) {
        ChatModel chatModel = createChatModel();

        NoMemoryAssistant noMemoryAssistant = createAssistantWithoutMemory(chatModel);
        runDemoWithoutMemory(noMemoryAssistant);

        MemoryAssistant withMemory = createAssistantWithMemory(chatModel);
        runDemoWithMemory(withMemory);
    }

    private static NoMemoryAssistant createAssistantWithoutMemory(ChatModel chatModel) {
        System.out.println("=== Option A: No Memory (Problem Demo) ===");
        return AiServices.builder(NoMemoryAssistant.class)
                .chatModel(chatModel)
                .build();
    }

    private static MemoryAssistant createAssistantWithMemory(ChatModel chatModel) {
        System.out.println("=== Option B: With Memory (ChatMemory) ===");
        return AiServices.builder(MemoryAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(createMemoryProvider())
                .build();
    }

    private static void runDemoWithoutMemory(NoMemoryAssistant NoMemoryAssistant) {
        String r1 = NoMemoryAssistant.chat("My name is Xiaoming");
        System.out.println("Message 1: My name is Xiaoming");
        System.out.println("AI reply: " + r1);

        String r2 = NoMemoryAssistant.chat("What is my name?");
        System.out.println("Message 2: What is my name?");
        System.out.println("AI reply: " + r2);
        System.out.println("↑ Note: AI may not remember your name (no memory)\n");
    }

    private static ChatMemoryProvider createMemoryProvider() {
        return new ChatMemoryProvider() {
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
    }

    private static void runDemoWithMemory(MemoryAssistant withMemory) {
        runUserSessionA(withMemory);
        runUserSessionB(withMemory);
    }

    private static void runUserSessionA(MemoryAssistant withMemory) {
        System.out.println("--- Session 1 (User A) ---");
        String a1 = withMemory.chat("user-A", "My name is Xiaoming, and I am a Java developer");
        System.out.println("User A: My name is Xiaoming, and I am a Java developer");
        System.out.println("AI: " + a1);

        String a2 = withMemory.chat("user-A", "What is my name? What do I do?");
        System.out.println("User A: What is my name? What do I do?");
        System.out.println("AI: " + a2);
        System.out.println("↑ AI remembers user A's information\n");
    }

    private static void runUserSessionB(MemoryAssistant withMemory) {
        System.out.println("--- Session 2 (User B, independent session) ---");
        String b1 = withMemory.chat("user-B", "My name is Xiaohong, and I am a front-end engineer");
        System.out.println("User B: My name is Xiaohong, and I am a front-end engineer");
        System.out.println("AI: " + b1);

        String b2 = withMemory.chat("user-B", "What is my name?");
        System.out.println("User B: What is my name?");
        System.out.println("AI: " + b2);
        System.out.println("↑ User B's session is independent, won't confuse with user A's information\n");
    }

    private static ChatModel createChatModel() {
        Map<String, Object> config = loadConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            System.err.println("Please configure API Key first! See step-00-setup");
            System.exit(1);
        }

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream is = MemoryDemo.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml not found");
        }
        return yaml.load(is);
    }
}