package com.tutorial;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.SystemMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * Step 02: Streaming output - Real-time Token-level response.
 *
 * <p>
 * Key concepts:
 * - StreamingChatModel: Streaming version of ChatModel, returns token by token
 * - TokenStream: Streaming return type from AiServices, handled via callbacks
 * - onPartialResponse: Triggered when receiving each token
 * - onCompleteResponse: Triggered after all tokens are received
 * - onError: Triggered when an error occurs
 * <p>
 *
 * Why streaming?
 * <p>
 * - Lower perceived latency (first token ~200ms vs full response ~5s)
 * - Frontend can implement typewriter effect (shown in Step 08)
 */
public class StreamingDemo {

    @SystemMessage("You are a seasoned Java development mentor. Keep responses concise, accurate, and include code examples.")
    interface StreamingAssistant {
        TokenStream chat(String message);
    }

    public static void main(String[] args) {
        StreamingChatModel streamingModel = createStreamingModel();

        StreamingAssistant assistant = AiServices
                .builder(StreamingAssistant.class)
                .streamingChatModel(streamingModel)
                .build();

        // Single streaming demo
        System.out.println("=== Streaming Demo ===");
        System.out.println("Ask: Introduce Spring Boot in 100 characters");
        System.out.print("Answer: ");

        // Create a CountDownLatch with initial count of 1
        // This is used to wait for the async streaming to complete
        CountDownLatch latch = new CountDownLatch(1);
        
        // Counter for tracking output tokens
        final int[] outputTokenCount = {0};

        // Start streaming chat - callbacks run on background threads
        String question = "Introduce Spring Boot in 100 characters";
        System.out.println("[Stats] Input tokens: " + question.length());  // Approximate input token count
        
        assistant.chat(question)
                .onPartialResponse(token -> {
                    System.out.print(token);
                    outputTokenCount[0]++;  // Increment token counter
                })
                .onCompleteResponse(response -> {
                    System.out.println("\n--- Completed ---");
                    System.out.println("[Stats] Output tokens: " + outputTokenCount[0]);
                    latch.countDown();  // Decrement count to 0, unblock main thread
                })
                .onError(error -> {
                    System.err.println("\nError: " + error.getMessage());
                    latch.countDown();  // Also unblock on error
                })
                .start();  // Trigger the streaming request

        // Block main thread until latch.countDown() is called
        // This prevents the program from exiting before streaming completes
        awaitLatch(latch);
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            // 阻塞当前线程直到计数器=0
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static StreamingChatModel createStreamingModel() {
        Map<String, Object> config = loadConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            System.err.println("Please configure API Key first! Refer to step-00-setup");
            System.exit(1);
        }

        return OpenAiStreamingChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream is = StreamingDemo.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml not found");
        }
        return yaml.load(is);
    }
}
