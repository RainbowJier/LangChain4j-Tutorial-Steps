package com.tutorial.config;

import com.tutorial.FullAssistant;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ProviderConfig {
    public static Map<String, String> loadProviderConfig() {
        Map<String, Object> config = loadConfig();
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
        return providerConfig;
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream is = ProviderConfig.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml not found");
        }
        return yaml.load(is);
    }
}
