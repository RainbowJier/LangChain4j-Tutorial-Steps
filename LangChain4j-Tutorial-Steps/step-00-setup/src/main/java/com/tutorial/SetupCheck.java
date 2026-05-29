package com.tutorial;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Step 00: Environment Setup — verify LLM API connectivity.
 * <p>
 * This program does the following:
 * 1. Read the LLM configuration from application.yml.
 * 2. Create ChatModel based on configuration.
 * 3. Send a test message to verify whether API is valid.
 * 4. Print environment check results.
 */
@Slf4j
public class SetupCheck {

    public static void main(String[] args) {
        log.info("=== LangChain4j Teaching environment inspection ===\n");

        // 1. Read configuration.
        Map<String, Object> config = loadConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        if (llmConfig == null) {
            log.error("'llm' section not found in application.yml");
            return;
        }
        String provider = (String) llmConfig.get("provider");
        if (provider == null) {
            log.error("'llm.provider' not configured in application.yml");
            return;
        }
        log.info("Configuration Provider: " + provider);

        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);
        String apiKey = providerConfig.get("api-key");
        if (apiKey == null) apiKey = "";
        String modelName = providerConfig.get("model-name");
        String baseUrl = providerConfig.get("base-url");

        log.info("API Key: " + (apiKey.length() > 8 ? apiKey.substring(0, 8) + "..." : "(not set)"));
        log.info("Model: " + modelName);
        log.info("Base URL: " + baseUrl);

        // 2. Try Connecting.
        log.info("Connecting to LLM API...");
        try {
            ChatModel chatModel = OpenAiChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();

            String response = chatModel.chat("Please reply OK");

            log.info("Connection successful！");
            log.info("LLM response: " + response.trim());
            log.info("✅ Environment check passed! you are now already to study.");
            log.info("Next Step: cd ../step-01-hello-llm");
        } catch (Exception e) {
            log.info("Connection Failure！");
            log.info("Error: " + e.getMessage());
            log.info("Common Causes: ");
            log.info(" - Invalid or Expired API Key");
            log.info(" - Network can not access to the API address.");
            log.info(" - Insufficient account balance");
        }
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream is = SetupCheck.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (is == null) {
                throw new IllegalStateException("application.yml not found");
            }
            return yaml.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.yml", e);
        }
    }
}
