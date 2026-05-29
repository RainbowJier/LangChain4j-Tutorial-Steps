package com.tutorial;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ProviderConfig {
    @SuppressWarnings("unchecked")
    public static Map<String, String> loadProviderConfig() {
        Map<String, Object> config = loadConfig();
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        if (llmConfig == null) {
            throw new IllegalStateException("'llm' section not found in application.yml");
        }
        String provider = (String) llmConfig.get("provider");
        if (provider == null) {
            throw new IllegalStateException("'llm.provider' not configured in application.yml");
        }
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

        String apiKey = providerConfig.get("api-key");
        if (apiKey == null || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException("Please configure API Key first! See step-00-setup");
        }
        return providerConfig;
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        try (InputStream is = ProviderConfig.class.getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (is == null) {
                throw new IllegalStateException("application.yml not found");
            }
            return yaml.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.yml", e);
        }
    }
}
