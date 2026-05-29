package com.tutorial;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/**
 * Step 01: Hello LLM - The simplest LLM invocation.
 * <p>
 * Demonstrate two calling methods:
 * 1. Directly call ChatModel.chat() - the underlying API
 * 2. Through the AiServices interface - recommended, supporting advanced features such as @SystemMessage
 * <p>
 * Key Concepts:
 * - ChatModel: The core interface for interacting with LLM, used for sending messages and receiving responses
 * - AiServices: Declarative AI services, defined by interfaces and annotations for specifying behaviors
 * - @SystemMessage: Defines the role and behavior rules of the AI
 */
public class HelloLlm {

    // ===== 方式 A 的接口（无系统消息） =====
    interface SimpleAssistant {
        String chat(String message);
    }

    // ===== 方式 B 的接口（带系统消息） =====
    @SystemMessage("You are a seasoned Java development mentor. Your response should be accurate and concise, and include code examples.")
    interface MentorAssistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        ChatModel chatModel = createChatModel();

        System.out.println("\n=== Method A：Directly call ChatModel ===");
        String question1 = "Introduce Java in one sentence.";
        System.out.println("Ask: " + question1);
        String answer = chatModel.chat(question1);
        System.out.println("Response: " + answer);


        System.out.println("\n=== Methos B：AiServices interface（No SystemMessage） ===");
        SimpleAssistant simple = AiServices.builder(SimpleAssistant.class)
                .chatModel(chatModel)
                .build();
        String question2 = "What is RAG ?";
        System.out.println("Ask: " + question2);
        String answer2 = simple.chat(question2);
        System.out.println("Response: " + answer2);


        System.out.println("\n=== Method C：AiServices interface（@SystemMessage） ===");
        MentorAssistant mentor = AiServices.builder(MentorAssistant.class)
                .chatModel(chatModel)
                .build();
        String question3 = "What is RAG ?";
        System.out.println("Ask: " + question3);
        String answer3 = mentor.chat(question3);
        System.out.println("Response: " + answer3);
    }

    // ===== 工具方法：从 application.yml 加载配置并创建 ChatModel =====
    private static ChatModel createChatModel() {
        Map<String, Object> config = loadConfig();

        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        if (llmConfig == null) {
            throw new IllegalStateException("'llm' section not found in application.yml");
        }
        String provider = (String) llmConfig.get("provider");
        if (provider == null) {
            throw new IllegalStateException("'llm.provider' not configured in application.yml");
        }
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException("Please configure API Key first! refer to step-00-setup");
        }

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream is = HelloLlm.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (is == null) {
                throw new IllegalStateException("application.yml not found");
            }
            return yaml.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.yml", e);
        }
    }
}
